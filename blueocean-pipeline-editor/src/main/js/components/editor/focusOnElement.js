/**
 * Attempts to focus on the element found by the provided selector
 */
export default function focusOnElement(selector: string) {
    let tries = 0;
    const focusFirstField = () => {
        try {
            document.querySelector(selector).focus();
        } catch(e) {
            tries++;
            if(tries < 5) {
                setTimeout(focusFirstField, 500);
            }
        }
    };
    setTimeout(focusFirstField, 200);
}
