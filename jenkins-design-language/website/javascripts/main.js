(function($){
    $(document).ready(function() {

        // Set the size of the JDL storybook frame.
        var jdlStorybookFrame = $('.jdl-storybook');
        if (jdlStorybookFrame.length === 1) {
            var $window = $(window);

            function setStoryBookFrameHeight() {
                var offset = jdlStorybookFrame.offset();
                jdlStorybookFrame.height($window.height() - offset.top - 40);
            }
            $window.resize(setStoryBookFrameHeight);

            setStoryBookFrameHeight();
            
            function waitForIFrameContent(selector, iframe, onVisible, checkFreq = 10) {
                setTimeout(function() {
                    var content = $(selector, iframe.contents());
                    if (content.length > 0) {
                        onVisible(content);
                    } else {
                        waitForIFrameContent(selector, iframe, onVisible);
                    }
                }, checkFreq);
            }

            function removeActionLogger() {
                waitForIFrameContent('.Pane.horizontal.Pane2', jdlStorybookFrame, function(actionLogger) {
                    actionLogger.remove();
                    injectComponentDoc();
                });
            }
            function injectComponentDoc(checkFreq = 10) {
                var sourceIframe = $('iframe', jdlStorybookFrame.contents());
                waitForIFrameContent('span.componentdoc', sourceIframe, function(componentDocs) {
                    componentDocs.each(function() {
                        var componentDoc = $(this);
                        var docFile = componentDoc.attr('data-docfile');

                        if (docFile) {
                            var componentDocIframe = $('<iframe>');
                            componentDocIframe.attr('src', '../component-docs/' + docFile);
                            componentDocIframe.addClass('componentdoc');
                            componentDoc.replaceWith(componentDocIframe);
                        } else {
                            componentDoc.remove();
                        }
                    });
                    injectComponentDoc(100);
                }, checkFreq);
            }
            removeActionLogger();
        }

        // Style the nav buttons using JDL styles
        var currentUrl = window.location.pathname.replace('/jenkins-design-language', ''); // yeah, hacky
        if (currentUrl === '' || currentUrl === '/index') {
            currentUrl = '/';
        }
        $('.nav a').addClass('btn-secondary inverse').each(function() {
            var a = $(this);
            var ahref = a.attr('href');

            if (ahref.indexOf('./') === 0) {
                ahref = ahref.substring(1);
            }
            if (currentUrl === ahref) {
                a.removeClass('btn-secondary').addClass('btn');
            }
        });
        
        // prettify code
        var codeBlocks = $('pre code.language-html');
        if (codeBlocks.length > 0) {
            codeBlocks.each(function() {
                var code = $(this);
                var pre = code.parent();
                
                // Make sure the immediate parent is a pre
                if (pre.prop("tagName").toLowerCase() === 'pre') {
                    var theCode = code.text();
                    pre.empty();
                    pre.text(theCode);
                    pre.addClass('prettyprint');
                    pre.addClass('lang-html');
                }
            });
            prettyPrint();
        }
    });
}(jQuery));
