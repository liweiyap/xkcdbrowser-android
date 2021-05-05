package com.liweiyap.xkcdbrowser.json;

public class JsonDataModel
{
    public JsonDataModel(){}

    public String getComicImageUrl()
    {
        return comicImageUrl;
    }

    public Integer getComicNum()
    {
        return comicNum;
    }

    public String getComicTitle()
    {
        return comicTitle;
    }

    public String getComicDay()
    {
        return comicDay;
    }

    public String getComicMonth()
    {
        return comicMonth;
    }

    public String getComicYear()
    {
        return comicYear;
    }

    public String getComicAltText()
    {
        return comicAltText;
    }

    public void setComicImageUrl(String comicImageUrl)
    {
        this.comicImageUrl = comicImageUrl;
    }

    public void setComicNum(Integer comicNum)
    {
        this.comicNum = comicNum;
    }

    public void setComicTitle(String comicTitle)
    {
        this.comicTitle = comicTitle;
    }

    public void setComicDay(String comicDay)
    {
        this.comicDay = comicDay;
    }

    public void setComicMonth(String comicMonth)
    {
        this.comicMonth = comicMonth;
    }

    public void setComicYear(String comicYear)
    {
        this.comicYear = comicYear;
    }

    public void setComicAltText(String comicAltText)
    {
        this.comicAltText = comicAltText;
    }

    private String comicImageUrl;
    private Integer comicNum;
    private String comicTitle;
    private String comicDay;
    private String comicMonth;
    private String comicYear;
    private String comicAltText;
}