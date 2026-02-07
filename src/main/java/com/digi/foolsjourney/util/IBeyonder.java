package com.digi.foolsjourney.util;

public interface IBeyonder {
    int getSequence();
    void setSequence(int sequence);

    double getSpirituality();
    void setSpirituality(double spirituality);

    boolean isSpiritVisionActive();
    void setSpiritVision(boolean active);

    int getCooldown();
    void setCooldown(int ticks);

    void syncBeyonderData();

    double getDigestion();
    void setDigestion(double digestion);
    void addDigestion(double amount);
}