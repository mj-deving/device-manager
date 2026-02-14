package com.mj.portfolio.client.model;

public class Device {

    private String id;
    private String name;
    private DeviceType type;
    private DeviceStatus status;
    private String ipAddress;
    private String location;
    private String createdAt;
    private String updatedAt;

    public Device() {}

    public String getId()           { return id; }
    public void setId(String id)    { this.id = id; }

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public DeviceType getType()             { return type; }
    public void setType(DeviceType type)    { this.type = type; }

    public DeviceStatus getStatus()                 { return status; }
    public void setStatus(DeviceStatus status)      { this.status = status; }

    public String getIpAddress()                { return ipAddress; }
    public void setIpAddress(String ipAddress)  { this.ipAddress = ipAddress; }

    public String getLocation()             { return location; }
    public void setLocation(String l)       { this.location = l; }

    public String getCreatedAt()            { return createdAt; }
    public void setCreatedAt(String c)      { this.createdAt = c; }

    public String getUpdatedAt()            { return updatedAt; }
    public void setUpdatedAt(String u)      { this.updatedAt = u; }

    @Override
    public String toString() { return name != null ? name : ""; }
}
