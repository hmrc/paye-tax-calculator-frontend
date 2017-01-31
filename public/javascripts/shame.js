// slightly modified https://github.com/hmrc/assets-frontend/blob/master/assets/javascripts/modules/toggle.js
// to be deleted once PR: https://github.com/hmrc/assets-frontend/pull/747/files is merged

(function() {

    var $toggleElems

    var toggleEvent = function ($elem) {
        var triggerSelector = $elem.data('trigger')
        var $triggerElemSelector = $(triggerSelector[0] === '.' ? triggerSelector : '.' + triggerSelector)
        var targetSelector = $elem.data('target')

        // toggle single target
        if (targetSelector) {
            var openId = $elem.data('open')
            var closeId = $elem.data('close')
            var $target = $('#' + targetSelector)
            $elem.on('click', $triggerElemSelector, function (event) {
                // limit to left mouse click and space bar
                if (event.which === 1 || 32) {
                    if (event.target.id === openId) {
                        show($target)
                    } else if (event.target.id === closeId) {
                        hide($target)
                    }
                }
            })

            // toggle multiple targets
        } else {
            var $inputs = $elem.find('[data-target]')
            $elem.on('click', $triggerElemSelector, function (event) {
                // limit to left mouse click and space bar
                if (event.which === 1 || 32) {
                    var $input = $(event.target)
                    if ($input.prop('tagName') === 'LABEL') $input = $input.find('input').first()
                    if ($input.prop('tagName') === 'INPUT' && $input.data('target')) {
                        show($('#' + $input.data('target')))
                        $inputs.not($input).each(function (i, input) {
                            hide($('#' + $(input).data('target')))
                        })
                    }
                }
            })
        }
    }

    var show = function ($element) {
        $element.removeClass('hidden').attr('aria-expanded', 'true').attr('aria-visible', 'true')
    }

    var hide = function ($element) {
        $element.addClass('hidden').attr('aria-expanded', 'false').attr('aria-visible', 'false')
    }

    var addListeners = function () {
        $toggleElems.each(function (index, elem) {
            toggleEvent($(elem))
        })
    }

    var setup = function () {
        $toggleElems = $('.js-toggle-shame')
    }

    setup()

    if ($toggleElems.length) {
        addListeners()
    }

})();



