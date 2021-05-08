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

    public void setComicImageUrl(final String comicImageUrl)
    {
        this.comicImageUrl = comicImageUrl;
    }

    public void setComicNum(final Integer comicNum)
    {
        this.comicNum = comicNum;
    }

    public void setComicTitle(final String comicTitle)
    {
        this.comicTitle = comicTitle;
    }

    public void setComicDay(final String comicDay)
    {
        this.comicDay = comicDay;
    }

    public void setComicMonth(final String comicMonth)
    {
        this.comicMonth = comicMonth;
    }

    public void setComicYear(final String comicYear)
    {
        this.comicYear = comicYear;
    }

    public void setComicAltText(final String comicAltText)
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