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
//    private String local_id;
//
//    private String stationLatitude;
//
//    private String stationLongitude;
//
//    private String state;
//
//    private String stationId;
//
//    public String getLocal_id ()
//    {
//        return local_id;
//    }
//
//    public void setLocal_id (String local_id)
//    {
//        this.local_id = local_id;
//    }
//
//    public String getStationLatitude ()
//    {
//        return stationLatitude;
//    }
//
//    public void setStationLatitude (String stationLatitude)
//    {
//        this.stationLatitude = stationLatitude;
//    }
//
//    public String getStationLongitude ()
//    {
//        return stationLongitude;
//    }
//
//    public void setStationLongitude (String stationLongitude)
//    {
//        this.stationLongitude = stationLongitude;
//    }
//
//    public String getState ()
//    {
//        return state;
//    }
//
//    public void setState (String state)
//    {
//        this.state = state;
//    }
//
//    public String getStationId ()
//    {
//        return stationId;
//    }
//
//    public void setStationId (String stationId)
//    {
//        this.stationId = stationId;
//    }
//
//    @Override
//    public String toString()
//    {
//        return "Model__station [local_id = "+local_id+", stationLatitude = "+stationLatitude+", stationLongitude = "+stationLongitude+", state = "+state+", stationId = "+stationId+"]";
//    }
}
