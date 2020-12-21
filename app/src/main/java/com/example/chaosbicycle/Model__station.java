package com.example.chaosbicycle;

public class Model__station {
    private String stationLatitude;

    private String stationLongitude;

    private String stationName;

    private Integer shared;

    private Integer rackTotCnt;

    private Integer predict;

    private Integer parkingBikeTotCnt;

    private Integer stationNumber;


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

    public Integer getShared ()
    {
        return shared;
    }

    public void setShared (Integer shared)
    {
        this.shared = shared;
    }

    public Integer getRackTotCnt ()
    {
        return rackTotCnt;
    }

    public void setRackTotCnt (Integer rackTotCnt)
    {
        this.rackTotCnt = rackTotCnt;
    }

    public Integer getPredict ()
    {
        return predict;
    }

    public void setPredict (Integer predict)
    {
        this.predict = predict;
    }

    public Integer getParkingBikeTotCnt (){ return parkingBikeTotCnt; }

    public void setParkingBikeTotCnt (Integer parkingBikeTotCnt)
    {
        this.parkingBikeTotCnt = parkingBikeTotCnt;
    }

    public Integer getStationNumber (){ return stationNumber; }

    public void setStationNumber (Integer stationNumber)
    {
        this.stationNumber = stationNumber;
    }


    @Override
    public String toString()
    {
        return "ClassPojo [predict= "+predict+", parkingBikeTotCnt= "+parkingBikeTotCnt+", stationNumber= "+stationNumber+", rackTotCnt= "+rackTotCnt+", stationLatitude = "+stationLatitude+", stationLongitude = "+stationLongitude+", stationName = "+stationName+", shared = "+shared+"]";
    }
}
