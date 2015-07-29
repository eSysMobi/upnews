package mobi.esys.upnews_online.cbr;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="Valute")
public class Currency {
    @Element(name="CharCode")
    private String currCharCode;
    @Element(name="Value")
    private String currValue;
    @Element (name="Nominal")
    private String nominal;

    public Currency() {

    }

    public String getCurrCharCode() {
        return currCharCode;
    }

    public void setCurrCharCode(String currCharCode) {
        this.currCharCode = currCharCode;
    }


    public String getCurrValue() {
        return currValue;
    }


    public void setCurrValue(String currValue) {
        this.currValue = currValue;
    }

    public String getNominal() {
        return nominal;
    }

    @Override
    public String toString() {
        return "Currency{" + "currCharCode='" + currCharCode + '\'' + ", currValue='" + currValue + '\'' + '}';
    }
}
