package org.mangocube.corenut.commons.xom.betwixt;

import org.apache.commons.digester.RuleSetBase;
import org.apache.commons.digester.Digester;
import org.apache.commons.betwixt.digester.*;

class EsCommonRuleSet extends RuleSetBase {

    /**
     * Adds rule instances.
     */
    public void addRuleInstances(Digester digester) {
        digester.addRule("*/element", new EsElementRule());
        digester.addRule("*/text", new TextRule());
        digester.addRule("*/attribute", new AttributeRule());
        digester.addRule("*/hide", new HideRule());
        digester.addRule("*/addDefaults", new AddDefaultsRule());

        OptionRule optionRule = new OptionRule();
        digester.addRule("*/option", optionRule);
        digester.addRule("*/option/name", optionRule.getNameRule());
        digester.addRule("*/option/value", optionRule.getValueRule());
    }
}
