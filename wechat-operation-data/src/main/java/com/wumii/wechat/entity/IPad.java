package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.wumii.application.entity.IdEntity;
import com.wumii.application.util.DateTimeUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class IPad extends IdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigserial")
    private long id;

    @Column(nullable = false)
    private byte[] deviceId;

    @Column(nullable = false, length = 64)
    private String imei;

    @Column(nullable = false, length = 64)
    private String mac;

    @Column(nullable = false)
    private boolean occupied;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    @Column(columnDefinition = "timestamp with time zone")
    private Instant loginTime;

    private IPad() {}

    public IPad(byte[] deviceId, String imei, String mac) {
        this.deviceId = deviceId;
        this.imei = imei;
        this.mac = mac;
        this.occupied = false;
        this.creationTime = DateTimeUtils.now();
    }

    @Override
    public long getId() {
        return id;
    }

    public byte[] getDeviceId() {
        return deviceId;
    }

    public String getImei() {
        return imei;
    }

    public String getMac() {
        return mac;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Instant loginTime) {
        this.loginTime = loginTime;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPad iPad = (IPad) o;
        return id == iPad.id &&
                occupied == iPad.occupied &&
                Objects.equal(deviceId, iPad.deviceId) &&
                Objects.equal(imei, iPad.imei) &&
                Objects.equal(mac, iPad.mac) &&
                Objects.equal(creationTime, iPad.creationTime) &&
                Objects.equal(loginTime, iPad.loginTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, deviceId, imei, mac, occupied, creationTime, loginTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("deviceId", deviceId)
                .add("imei", imei)
                .add("mac", mac)
                .add("occupied", occupied)
                .add("creationTime", creationTime)
                .add("loginTime", loginTime)
                .toString();
    }
}
