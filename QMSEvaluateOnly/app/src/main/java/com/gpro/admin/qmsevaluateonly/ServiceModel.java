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
}
