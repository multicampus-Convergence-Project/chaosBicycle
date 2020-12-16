package com.example.chaosbicycle;

public class Model__station {
    private String stationLatitude;

    private String stationLongitude;

    private String stationName;

    private String parkingBikeTotCnt;

    public String getStationLatitude ()
    {
        return stationLatitude;
    }

    public void setStationLatitude (String stationLatitude)
    {
        this.stationLatitude = stationLatitude;
    }

    public String getStationLongitude ()
    {
        return stationLongitude;
    }

    public void setStationLongitude (String stationLongitude)
    {
        this.stationLongitude = stationLongitude;
    }

    public String getStationName ()
    {
        return stationName;
    }

    public void setStationName (String stationName)
    {
        this.stationName = stationName;
    }

    public String getParkingBikeTotCnt ()
    {
        return parkingBikeTotCnt;
    }

    public void setParkingBikeTotCnt (String parkingBikeTotCnt)
    {
        this.parkingBikeTotCnt = parkingBikeTotCnt;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [stationLatitude = "+stationLatitude+", stationLongitude = "+stationLongitude+", stationName = "+stationName+", parkingBikeTotCnt = "+parkingBikeTotCnt+"]";
    }
}
