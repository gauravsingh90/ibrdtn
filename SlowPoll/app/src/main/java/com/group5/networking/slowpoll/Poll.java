package com.group5.networking.slowpoll;

/**
 * Created by Joe on 4/24/2018.
 */

public class Poll {
    public String title;
    public String optionOne;
    public String optionTwo;
    public int responseOne;
    public int responseTwo;
    public String incentive;

    public Poll() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Poll(String title, String optionOne, String optionTwo, String incentive) {
        this.title = title;
        this.optionOne = optionOne;
        this.optionTwo = optionTwo;
        this.responseOne = 0;
        this.responseTwo = 0;
        this.incentive = incentive;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOptionOne() {
        return optionOne;
    }

    public void setOptionOne(String optionOne) {
        this.optionOne = optionOne;
    }

    public String getOptionTwo() {
        return optionTwo;
    }

    public void setOptionTwo(String optionTwo) {
        this.optionTwo = optionTwo;
    }

    public int getResponseOne() {
        return responseOne;
    }

    public void setResponseOne(int responseOne) {
        this.responseOne = responseOne;
    }

    public int getResponseTwo() {
        return responseTwo;
    }

    public void setResponseTwo(int responseTwo) {
        this.responseTwo = responseTwo;
    }

    public String getIncentive() {
        return incentive;
    }

    public void setIncentive(String incentive) {
        this.incentive = incentive;
    }

}
