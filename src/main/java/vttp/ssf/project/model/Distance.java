package vttp.ssf.project.model;

public class Distance {

    private Double distance;

    private String startAddress;
    private String endAddress;
    private String endName;
    private String endPostalCode;

    private Double latEnd;
    private Double lngEnd;

    public Distance() {}

    public Distance(Double distance, String startAddress, String endAddress, String endName, String endPostalCode,
            Double latEnd, Double lngEnd) {
        this.distance = distance;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.endName = endName;
        this.endPostalCode = endPostalCode;
        this.latEnd = latEnd;
        this.lngEnd = lngEnd;
    }


    public String getEndPostalCode() {return endPostalCode;}
    public void setEndPostalCode(String endPostalCode) {this.endPostalCode = endPostalCode;}

    public Double getDistance() {return distance;}
    public void setDistance(Double distance) {this.distance = distance;}

    public String getStartAddress() {return startAddress;}
    public void setStartAddress(String startAddress) {this.startAddress = startAddress;}

    public String getEndAddress() {return endAddress;}
    public void setEndAddress(String endAddress) {this.endAddress = endAddress;}

    public String getEndName() {return endName;}
    public void setEndName(String endName) {this.endName = endName;}

    public Double getLatEnd() {return latEnd;}
    public void setLatEnd(Double latEnd) {this.latEnd = latEnd;}

    public Double getLngEnd() {return lngEnd;}
    public void setLngEnd(Double lngEnd) {this.lngEnd = lngEnd;}

    @Override
    public String toString() {
        return "Distance [distance=" + distance + ", startAddress=" + startAddress + ", endAddress=" + endAddress
                + ", endName=" + endName + ", endPostalCode=" + endPostalCode + ", latEnd=" + latEnd + ", lngEnd="
                + lngEnd + "]";
    }

    





    



    

    
    
}
