
/* - input-label.js - */
// http://www.incunabulum.de/portal_javascripts/input-label.js?original=1
var ploneInputLabel={focus: function(e){var t=jq(e.target);if(t.hasClass('inputLabelActive')&&t.val()==t.attr('title'))
t.val('').removeClass('inputLabelActive')},blur: function(e){var t=jq(e.target);if(!t.val())
t.addClass('inputLabelActive').val(t.attr('title'))},submit: function(e){jq('input[title].inputLabelActive').filter(function(){return jq(this).val()==this.title}).val('').removeClass('inputLabelActive')}};jq(function(){jq('form:has(input[title].inputLabel)').submit(ploneInputLabel.submit);jq('input[title].inputLabel').each(function(){jq(this).focus(ploneInputLabel.focus).blur(ploneInputLabel.blur);if(!jq(this).val())
jq(this).val(this.title).removeClass('inputLabel').addClass('inputLabelActive')})});
