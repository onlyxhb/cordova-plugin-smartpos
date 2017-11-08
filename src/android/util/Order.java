package com.example.plugin.util;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-11-07.
 */

public class Order {
    private String saleOrder;
    private String customeName;
    private String advanceMoney;
    private String saleDate;
    private String deliverDate;
    private String saleAmount;
    private String saleMoney;
    private String sponerName;
    private ArrayList<Goods> goodses=new ArrayList<Goods>();
    private String printTime;

    public String getSaleOrder() {
        return saleOrder;
    }

    public void setSaleOrder(String saleOrder) {
        this.saleOrder = saleOrder;
    }

    public String getCustomeName() {
        return customeName;
    }

    public void setCustomeName(String customeName) {
        this.customeName = customeName;
    }

    public String getAdvanceMoney() {
        return advanceMoney;
    }

    public void setAdvanceMoney(String advanceMoney) {
        this.advanceMoney = advanceMoney;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public String getDeliverDate() {
        return deliverDate;
    }

    public void setDeliverDate(String deliverDate) {
        this.deliverDate = deliverDate;
    }

    public String getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(String saleAmount) {
        this.saleAmount = saleAmount;
    }

    public String getSaleMoney() {
        return saleMoney;
    }

    public void setSaleMoney(String saleMoney) {
        this.saleMoney = saleMoney;
    }

    public String getSponerName() {
        return sponerName;
    }

    public void setSponerName(String sponerName) {
        this.sponerName = sponerName;
    }

    public ArrayList<Goods> getGoodses() {
        return goodses;
    }

    public void setGoodses(ArrayList<Goods> goodses) {
        this.goodses = goodses;
    }

    public String getPrintTime() {
        return printTime;
    }

    public void setPrintTime(String printTime) {
        this.printTime = printTime;
    }

    public class Goods{
        private String Name;
        private String color;
        private String size;
        private String wholesalePrice;
        private String Amount;
        private String discount;
        private String Zmoney;

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getWholesalePrice() {
            return wholesalePrice;
        }

        public void setWholesalePrice(String wholesalePrice) {
            this.wholesalePrice = wholesalePrice;
        }

        public String getAmount() {
            return Amount;
        }

        public void setAmount(String amount) {
            Amount = amount;
        }

        public String getDiscount() {
            return discount;
        }

        public void setDiscount(String discount) {
            this.discount = discount;
        }

        public String getZmoney() {
            return Zmoney;
        }

        public void setZmoney(String zmoney) {
            Zmoney = zmoney;
        }
    }
}

