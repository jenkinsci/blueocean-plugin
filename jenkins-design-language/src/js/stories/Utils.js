const styleId = 'storybook-css';

/**
 * Creates a style rule.
 * Usage:
 *      createCssRule(
 *          '.red-bold',
 *          'color: #f00; font-weight: bold'
 *      )
 * @param selector
 * @param rules
 */
function createCssRule(selector, ...rules) {
    let styles = document.getElementById(styleId);

    if (!styles) {
        styles = document.createElement('style');
        styles.type = 'text/css';
        styles.id = styleId;
        document.getElementsByTagName('head')[0].appendChild(styles);
    }

    const sheet = styles.sheet;

    // add each rule via 'insertRule' if that rule doesn't already exist
    // (needed due to storybook / webpack hot reloading)
    for (let i = 0; i < rules.length; i++) {
        const rule = rules[i];
        const cssText = `${selector} { ${rule}; }`;
        let found = false;

        for (let j = 0; j < sheet.cssRules.length; j++) {
            const existingRule = sheet.cssRules[j];
            if (cssText === existingRule.cssText) {
                found = true;
                break;
            }
        }

        if (!found) {
            // insertRule won't allow semicolons
            sheet.insertRule(cssText.replace(';', ''), 0);
        }
    }
}


export default {
    createCssRule,
};
