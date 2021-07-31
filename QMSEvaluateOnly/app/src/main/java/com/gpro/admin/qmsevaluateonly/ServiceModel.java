package com.gpro.admin.qmsevaluateonly;

public class ServiceModel {
    String ServiceName;
    String TimeServe;
    int    Id;
    public ServiceModel(String _serviceName,String _timeServe,   int _id) {
        ServiceName = _serviceName;
        TimeServe = _timeServe;
        Id = _id;
    }

    /**
     * Pay attention here, you have to override the toString method as the
     * ArrayAdapter will reads the toString of the given object for the name
     *
     * @return ServiceName
     */
    @Override
    public String toString() {
        return ServiceName;
    }
}
