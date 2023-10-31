package application.api;

import application.api.timeoffs.Timeoffs;

public class TemporaryMain {
    public static void main(String[] args)  {
        try {
            Timeoffs timeoffs = new Timeoffs();
            timeoffs.approveTimeOff("6");
            timeoffs.approveTimeOff("7");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}