/*
 * BaseRouter.cpp
 *
 * Copyright (C) 2011 IBR, TU Braunschweig
 *
 * Written-by: Johannes Morgenroth <morgenroth@ibr.cs.tu-bs.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include "config.h"
#include "routing/BaseRouter.h"
#include "core/BundleCore.h"
#include "core/EventDispatcher.h"

#include "Configuration.h"
#include "net/TransferAbortedEvent.h"
#include "net/TransferCompletedEvent.h"
#include "net/BundleReceivedEvent.h"
#include "net/ConnectionEvent.h"
#include "routing/QueueBundleEvent.h"
#include "routing/RequeueBundleEvent.h"
#include "storage/BundleStorage.h"
#include "core/BundleGeneratedEvent.h"
#include "core/BundleExpiredEvent.h"
#include "core/BundleEvent.h"
#include "core/NodeEvent.h"
#include "core/TimeEvent.h"
#include "routing/NodeHandshakeEvent.h"
#include "routing/StaticRouteChangeEvent.h"

#include "routing/NodeHandshakeExtension.h"
#include "routing/RetransmissionExtension.h"

#include <ibrdtn/data/TrackingBlock.h>
#include <ibrdtn/data/ScopeControlHopLimitBlock.h>
#include <ibrdtn/utils/Clock.h>

#include <ibrcommon/Logger.h>
#include <ibrcommon/thread/MutexLock.h>

#ifdef WITH_BUNDLE_SECURITY
#include "security/SecurityManager.h"
#endif

namespace dtn
{
	namespace routing
	{
		BaseRouter *BaseRouter::Extension::_router = NULL;

		/**
		 * base implementation of the Extension class
		 */
		BaseRouter::Extension::Extension(dtn::storage::BundleSeeker &s)
		 : _seeker(s)
		{ }

		BaseRouter::Extension::~Extension()
		{ }

		BaseRouter& BaseRouter::Extension::operator*()
		{
			return *_router;
		}

		/**
		 * Transfer one bundle to another node.
		 * @param destination The EID of the other node.
		 * @param id The ID of the bundle to transfer. This bundle must be stored in the storage.
		 */
		void BaseRouter::Extension::transferTo(const dtn::data::EID &destination, const dtn::data::BundleID &id)
		{
			// lock the list of neighbors
			ibrcommon::MutexLock l(_router->getNeighborDB());

			// get the neighbor entry for the next hop
			NeighborDatabase::NeighborEntry &entry = _router->getNeighborDB().get(destination);

			// transfer bundle to the neighbor
			transferTo(entry, id);
		}

		void BaseRouter::Extension::transferTo(NeighborDatabase::NeighborEntry &entry, const dtn::data::BundleID &id)
		{
			// acquire the transfer of this bundle, could throw already in transit or no resource left exception
			entry.acquireTransfer(id);

			try {
				// transfer the bundle to the next hop
				dtn::core::BundleCore::getInstance().transferTo(entry.eid, id);
			} catch (const dtn::core::P2PDialupException&) {
				// release the transfer
				entry.releaseTransfer(id);

				// and abort the query
				throw NeighborDatabase::NeighborNotAvailableException();
			}
		}

		bool BaseRouter::Extension::isRouting(const dtn::data::EID &eid)
		{
			if (eid.getApplication() == "routing")
			{
				return true;
			}
			else if ((eid.getScheme() == dtn::data::EID::CBHE_SCHEME) && (eid.getApplication() == "50"))
			{
				return true;
			}

			return false;
		}

		/**
		 * base implementation of the Endpoint class
		 */
		BaseRouter::Endpoint::Endpoint()
		{ }

		BaseRouter::Endpoint::~Endpoint()
		{ }

		/**
		 * implementation of the VirtualEndpoint class
		 */
		BaseRouter::VirtualEndpoint::VirtualEndpoint(dtn::data::EID name)
		 : _client(NULL), _name(name)
		{ }

		BaseRouter::VirtualEndpoint::~VirtualEndpoint()
		{ }

		/**
		 * implementation of the BaseRouter class
		 */
		BaseRouter::BaseRouter(dtn::storage::BundleStorage &storage)
		 : _storage(storage), _nh_extension(NULL)
		{
			// register myself for all extensions
			Extension::_router = this;

			// add node handshake module
			_nh_extension = new dtn::routing::NodeHandshakeExtension(_storage);
			addExtension( _nh_extension );

			// add retransmission module
			addExtension( new dtn::routing::RetransmissionExtension(_storage) );
		}

		BaseRouter::~BaseRouter()
		{
			// delete all extensions
			for (std::list<BaseRouter::Extension*>::iterator iter = _extensions.begin(); iter != _extensions.end(); iter++)
			{
				delete (*iter);
			}
		}

		/**
		 * Add a routing extension to the routing core.
		 * @param extension
		 */
		void BaseRouter::addExtension(BaseRouter::Extension *extension)
		{
			_extensions.push_back(extension);
		}

		const std::list<BaseRouter::Extension*>& BaseRouter::getExtensions() const
		{
			return _extensions;
		}

		void BaseRouter::componentUp() throw ()
		{
			// routine checked for throw() on 15.02.2013
			dtn::core::EventDispatcher<dtn::net::TransferAbortedEvent>::add(this);
			dtn::core::EventDispatcher<dtn::net::TransferCompletedEvent>::add(this);
			dtn::core::EventDispatcher<dtn::net::BundleReceivedEvent>::add(this);
			dtn::core::EventDispatcher<dtn::routing::QueueBundleEvent>::add(this);
			dtn::core::EventDispatcher<dtn::routing::RequeueBundleEvent>::add(this);
			dtn::core::EventDispatcher<dtn::routing::NodeHandshakeEvent>::add(this);
			dtn::core::EventDispatcher<dtn::routing::StaticRouteChangeEvent>::add(this);
			dtn::core::EventDispatcher<dtn::core::NodeEvent>::add(this);
			dtn::core::EventDispatcher<dtn::core::BundleExpiredEvent>::add(this);
			dtn::core::EventDispatcher<dtn::core::TimeEvent>::add(this);
			dtn::core::EventDispatcher<dtn::core::BundleGeneratedEvent>::add(this);
			dtn::core::EventDispatcher<dtn::net::ConnectionEvent>::add(this);

			for (std::list<BaseRouter::Extension*>::iterator iter = _extensions.begin(); iter != _extensions.end(); iter++)
			{
				BaseRouter::Extension &ex = (**iter);
				ex.componentUp();
			}
		}

		void BaseRouter::componentDown() throw ()
		{
			// routine checked for throw() on 15.02.2013
			dtn::core::EventDispatcher<dtn::net::TransferAbortedEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::net::TransferCompletedEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::net::BundleReceivedEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::routing::QueueBundleEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::routing::RequeueBundleEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::routing::NodeHandshakeEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::routing::StaticRouteChangeEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::core::NodeEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::core::BundleExpiredEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::core::TimeEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::core::BundleGeneratedEvent>::remove(this);
			dtn::core::EventDispatcher<dtn::net::ConnectionEvent>::remove(this);

			// stop all extensions
			for (std::list<BaseRouter::Extension*>::iterator iter = _extensions.begin(); iter != _extensions.end(); iter++)
			{
				BaseRouter::Extension &ex = (**iter);
				ex.componentDown();
			}
		}

		/**
		 * method to receive new events from the EventSwitch
		 */
		void BaseRouter::raiseEvent(const dtn::core::Event *evt) throw ()
		{
			// If a new neighbor comes available, send him a request for the summary vector
			// If a neighbor went away we can free the stored database
			try {
				const dtn::core::NodeEvent &event = dynamic_cast<const dtn::core::NodeEvent&>(*evt);

				if (event.getAction() == NODE_AVAILABLE)
				{
					ibrcommon::MutexLock l(_neighbor_database);
					_neighbor_database.create( event.getNode().getEID() );
				}
				else if (event.getAction() == NODE_UNAVAILABLE)
				{
					ibrcommon::MutexLock l(_neighbor_database);
					_neighbor_database.create( event.getNode().getEID() ).reset();
				}

				// pass event to all extensions
				__forward_event(evt);
				return;
			} catch (const std::bad_cast&) { };

			try {
				const dtn::net::TransferCompletedEvent &event = dynamic_cast<const dtn::net::TransferCompletedEvent&>(*evt);

				// if a transfer is completed, then release the transfer resource of the peer
				try {
					// add this bundle to the purge vector if it is delivered to its destination
					if ((event.getBundle().procflags & dtn::data::Bundle::DESTINATION_IS_SINGLETON)
							&& ( event.getPeer().getNode() == event.getBundle().destination.getNode() ))
					{
						IBRCOMMON_LOGGER_DEBUG(20) << "singleton bundle added to purge vector: " << event.getBundle().toString() << IBRCOMMON_LOGGER_ENDL;

						// add it to the purge vector
						ibrcommon::MutexLock l(_purged_bundles_lock);
						_purged_bundles.add(event.getBundle());
					}

					// lock the list of neighbors
					ibrcommon::MutexLock l(_neighbor_database);
					NeighborDatabase::NeighborEntry &entry = _neighbor_database.get(event.getPeer());
					entry.releaseTransfer(event.getBundle());

					// add the bundle to the summary vector of the neighbor
					entry.add(event.getBundle());
				} catch (const NeighborDatabase::NeighborNotAvailableException&) { };

				// pass event to all extensions
				__forward_event(evt);
				return;
			} catch (const std::bad_cast&) { };

			try {
				const dtn::net::TransferAbortedEvent &event = dynamic_cast<const dtn::net::TransferAbortedEvent&>(*evt);

				// if a transfer is aborted, then release the transfer resource of the peer
				try {
					// lock the list of neighbors
					ibrcommon::MutexLock l(_neighbor_database);
					NeighborDatabase::NeighborEntry &entry = _neighbor_database.create(event.getPeer());
					entry.releaseTransfer(event.getBundleID());

					if (event.reason == dtn::net::TransferAbortedEvent::REASON_REFUSED)
					{
						const dtn::data::MetaBundle meta = _storage.get(event.getBundleID());

						// add the transferred bundle to the bloomfilter of the receiver
						entry.add(meta);
					}
				} catch (const dtn::storage::NoBundleFoundException&) { };

				// pass event to all extensions
				__forward_event(evt);
				return;
			} catch (const std::bad_cast&) { };

			try {
				const dtn::net::BundleReceivedEvent &received = dynamic_cast<const dtn::net::BundleReceivedEvent&>(*evt);

				// Store incoming bundles into the storage
				try {
					if (received.fromlocal)
					{
						// store the bundle into a storage module
						_storage.store(received.bundle);

						// set the bundle as known
						setKnown(received.bundle);

						// raise the queued event to notify all receivers about the new bundle
						QueueBundleEvent::raise(received.bundle, received.peer);
					}
					// if the bundle is not known
					else if (!filterKnown(received.bundle))
					{
						// security methods modifies the bundle, thus we need a copy of it
						dtn::data::Bundle bundle = received.bundle;

#ifdef WITH_BUNDLE_SECURITY
						// lets see if signatures and hashes are correct and remove them if possible
						dtn::security::SecurityManager::getInstance().verify(bundle);
#endif

						// increment value in the scope control hop limit block
						try {
							dtn::data::ScopeControlHopLimitBlock &schl = bundle.getBlock<dtn::data::ScopeControlHopLimitBlock>();
							schl.increment();
						} catch (const dtn::data::Bundle::NoSuchBlockFoundException&) { };

						// modify TrackingBlock
						try {
							dtn::data::TrackingBlock &track = bundle.getBlock<dtn::data::TrackingBlock>();
							track.append(dtn::core::BundleCore::local);
						} catch (const dtn::data::Bundle::NoSuchBlockFoundException&) { };

						// prevent loops
						try {
							ibrcommon::MutexLock l(_neighbor_database);

							// add the bundle to the summary vector of the neighbor
							_neighbor_database.get(received.peer).add(received.bundle);
						} catch (const NeighborDatabase::NeighborNotAvailableException&) { };

						// store the bundle into a storage module
						_storage.store(bundle);

						// raise the queued event to notify all receivers about the new bundle
						QueueBundleEvent::raise(received.bundle, received.peer);
					}
					else
					{
						IBRCOMMON_LOGGER_DEBUG(5) << "Duplicate bundle " << received.bundle.toString() << " from " << received.peer.getString() << " ignored." << IBRCOMMON_LOGGER_ENDL;
					}

					// finally create a bundle received event
					dtn::core::BundleEvent::raise(received.bundle, dtn::core::BUNDLE_RECEIVED);
#ifdef WITH_BUNDLE_SECURITY
				} catch (const dtn::security::SecurityManager::VerificationFailedException &ex) {
					IBRCOMMON_LOGGER(notice) << "Security checks failed (" << ex.what() << "), bundle will be dropped: " << received.bundle.toString() << IBRCOMMON_LOGGER_ENDL;
#endif
				} catch (const ibrcommon::IOException &ex) {
					IBRCOMMON_LOGGER(notice) << "Unable to store bundle " << received.bundle.toString() << IBRCOMMON_LOGGER_ENDL;

					// raise BundleEvent because we have to drop the bundle
					dtn::core::BundleEvent::raise(received.bundle, dtn::core::BUNDLE_DELETED, dtn::data::StatusReportBlock::DEPLETED_STORAGE);
				} catch (const dtn::storage::BundleStorage::StorageSizeExeededException &ex) {
					IBRCOMMON_LOGGER(notice) << "No space left for bundle " << received.bundle.toString() << IBRCOMMON_LOGGER_ENDL;

					// raise BundleEvent because we have to drop the bundle
					dtn::core::BundleEvent::raise(received.bundle, dtn::core::BUNDLE_DELETED, dtn::data::StatusReportBlock::DEPLETED_STORAGE);
				}

				// no routing extension should be interested in this event
				// everybody should listen to QueueBundleEvent instead
				return;
			} catch (const std::bad_cast&) { };

			try {
				const dtn::core::BundleGeneratedEvent &generated = dynamic_cast<const dtn::core::BundleGeneratedEvent&>(*evt);
				
				// set the bundle as known
				setKnown(generated.bundle);

				// Store incoming bundles into the storage
				try {
					// store the bundle into a storage module
					_storage.store(generated.bundle);

					// raise the queued event to notify all receivers about the new bundle
 					QueueBundleEvent::raise(generated.bundle, dtn::core::BundleCore::local);
				} catch (const ibrcommon::IOException &ex) {
					IBRCOMMON_LOGGER(notice) << "Unable to store bundle " << generated.bundle.toString() << IBRCOMMON_LOGGER_ENDL;
				} catch (const dtn::storage::BundleStorage::StorageSizeExeededException &ex) {
					IBRCOMMON_LOGGER(notice) << "No space left for bundle " << generated.bundle.toString() << IBRCOMMON_LOGGER_ENDL;
				}

				// do not pass this event to any extension
				return;
			} catch (const std::bad_cast&) { };

			try {
				const dtn::core::TimeEvent &time = dynamic_cast<const dtn::core::TimeEvent&>(*evt);
				size_t expire_time = time.getTimestamp();
				if (expire_time <= 60) expire_time = 0;
				else expire_time -= 60;

				{
					ibrcommon::MutexLock l(_known_bundles_lock);
					_known_bundles.expire(expire_time);
				}

				{
					ibrcommon::MutexLock l(_purged_bundles_lock);
					_purged_bundles.expire(expire_time);
				}

				{
					ibrcommon::MutexLock l(_neighbor_database);
					_neighbor_database.expire(time.getTimestamp());
				}

				// pass event to all extensions
				__forward_event(evt);
				return;
			} catch (const std::bad_cast&) { };

			// pass event to all extensions
			__forward_event(evt);
		}

		void BaseRouter::__forward_event(const dtn::core::Event *evt) const throw ()
		{
			// notify all underlying extensions
			for (std::list<BaseRouter::Extension*>::const_iterator iter = _extensions.begin(); iter != _extensions.end(); iter++)
			{
				(*iter)->notify(evt);
			}
		}

		/**
		 * Get a bundle out of the storage.
		 * @param id The ID of the bundle.
		 * @return The requested bundle.
		 */
		dtn::data::Bundle BaseRouter::getBundle(const dtn::data::BundleID &id)
		{
			return _storage.get(id);
		}

		void BaseRouter::doHandshake(const dtn::data::EID &eid)
		{
			_nh_extension->doHandshake(eid);
		}

		dtn::storage::BundleStorage& BaseRouter::getStorage()
		{
			return _storage;
		}

		void BaseRouter::setKnown(const dtn::data::MetaBundle &meta)
		{
			ibrcommon::MutexLock l(_known_bundles_lock);
			return _known_bundles.add(meta);
		}

		// set the bundle as known
		bool BaseRouter::isKnown(const dtn::data::BundleID &id)
		{
			ibrcommon::MutexLock l(_known_bundles_lock);
			return _known_bundles.has(id);
		}

		// check if a bundle is known
		// if the bundle is unkown add it to the known list and return
		// false
		bool BaseRouter::filterKnown(const dtn::data::MetaBundle &meta)
		{
			ibrcommon::MutexLock l(_known_bundles_lock);
			bool ret = _known_bundles.has(meta);
			if (!ret) _known_bundles.add(meta);

			return ret;
		}

		const dtn::data::BundleSet BaseRouter::getKnownBundles()
		{
			ibrcommon::MutexLock l(_known_bundles_lock);
			return _known_bundles;
		}

		void BaseRouter::addPurgedBundle(const dtn::data::MetaBundle &meta)
		{
			ibrcommon::MutexLock l(_purged_bundles_lock);
			return _purged_bundles.add(meta);
		}

		const dtn::data::BundleSet BaseRouter::getPurgedBundles()
		{
			ibrcommon::MutexLock l(_purged_bundles_lock);
			return _purged_bundles;
		}

		const std::string BaseRouter::getName() const
		{
			return "BaseRouter";
		}

		NeighborDatabase& BaseRouter::getNeighborDB()
		{
			return _neighbor_database;
		}
	}
}
