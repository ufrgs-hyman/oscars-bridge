package br.rnp.meican.oscars.domain;

import java.util.ArrayList;

/**
 * @author Maurício Quatrin Guerreiro
 */
public class Circuit {
    
    private String gri;
    private String user;
    private String description;
    private Integer bandwidth;
    private Long startTime;
    private Long endTime;
    private String status;
    private ArrayList<String> path;
    
    public Circuit() {
        this.path = new ArrayList<String>();
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGri() {
        return gri;
    }

    public void setGri(String gri) {
        this.gri = gri;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ArrayList<String> getPath() {
        return path;
    }

    public void setPath(ArrayList<String> path) {
        this.path = path;
    }

    public Integer getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Integer bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void addPoint(String point) {
        this.path.add(point);
    }
}
