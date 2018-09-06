package gov.nist.healthcare.utils.format;

import hl7.v2.instance.Element;
import hl7.v2.instance.Query;
import hl7.v2.instance.Simple;
import scala.collection.Iterator;
import scala.collection.immutable.List;

public abstract class CodedElementFormat {

    /**
     * Determines if a triplet is valid
     * 
     * @param code
     *        the identifier code
     * @param text
     *        the text associated
     * @param codeSystem
     *        the code system
     * @return true if triplet is valid
     */
    public abstract boolean isValid(String code, String text, String codeSystem);

    /**
     * The code system to check against
     * 
     * @return the code system to check against
     */
    public abstract String getCodeSystemValue();

    /**
     * Checks the first triplet (elements 1 to 3), only if the code system of
     * the element matches the code system of the format we want to check
     * against
     * 
     * @param elementList
     *        the list of complex coded element to check
     * @return true if first triplet is valid
     */
    public boolean checkFirstTriplet(List<Element> elementList) {
        Iterator<Element> it = elementList.iterator();
        while (it.hasNext()) {
            Element element = it.next();
            // get 3rd component
            List<Simple> codeSystemList = Query.queryAsSimple(element, "3[1]").get();
            String codeSystem = getValue(codeSystemList);
            if (getCodeSystemValue().equals(codeSystem)) {
                // get 1st component
                List<Simple> identifierList = Query.queryAsSimple(element,
                        "1[1]").get();
                String identifier = getValue(identifierList);

                // get 2nd component
                List<Simple> textList = Query.queryAsSimple(element, "2[1]").get();
                String text = getValue(textList);

                return isValid(identifier, text, codeSystem);
            }
        }
        return true;
    }

    /**
     * Checks the second triplet (elements 4 to 6), only if the code system of
     * the element matches the code system of the format we want to check
     * against
     * 
     * @param elementList
     *        the list of complex coded element to check
     * @return true if first triplet is valid
     */
    public boolean checkSecondTriplet(List<Element> elementList) {
        Iterator<Element> it = elementList.iterator();
        while (it.hasNext()) {
            Element element = it.next();
            // get 3rd component
            List<Simple> codeSystemList = Query.queryAsSimple(element, "6[1]").get();
            String codeSystem = getValue(codeSystemList);
            if (getCodeSystemValue().equals(codeSystem)) {
                // get 1st component
                List<Simple> identifierList = Query.queryAsSimple(element,
                        "4[1]").get();
                String identifier = getValue(identifierList);

                // get 2nd component
                List<Simple> textList = Query.queryAsSimple(element, "5[1]").get();
                String text = getValue(textList);

                return isValid(identifier, text, codeSystem);
            }
        }
        return true;
    }

    private String getValue(List<Simple> simpleElementList) {
        if (simpleElementList.size() > 1) {
            throw new IllegalArgumentException("Invalid List size : "
                    + simpleElementList.size());
        }
        if (simpleElementList.size() == 0) {
            return "";
        }
        // only get first element
        return simpleElementList.apply(0).value().raw();
    }

}
