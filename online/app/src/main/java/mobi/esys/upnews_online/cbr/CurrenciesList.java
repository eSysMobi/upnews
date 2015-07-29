package mobi.esys.upnews_online.cbr;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "ValCurs")
public class CurrenciesList {
    @ElementList(inline=true, name="Valute")
    public List<Currency> currencies;
}
