!function(t){function e(r){if(n[r])return n[r].exports;var o=n[r]={exports:{},id:r,loaded:!1};return t[r].call(o.exports,o,o.exports,e),o.loaded=!0,o.exports}var n={};return e.m=t,e.c=n,e.p="",e(0)}([function(t,e,n){t.exports=n(40)},,,function(t,e){"use strict";e.__esModule=!0,e["default"]=function(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}},function(t,e,n){"use strict";function r(t){return t&&t.__esModule?t:{"default":t}}e.__esModule=!0;var o=n(5),i=r(o);e["default"]=function(){function t(t,e){for(var n=0;n<e.length;n++){var r=e[n];r.enumerable=r.enumerable||!1,r.configurable=!0,"value"in r&&(r.writable=!0),(0,i["default"])(t,r.key,r)}}return function(e,n,r){return n&&t(e.prototype,n),r&&t(e,r),e}}()},function(t,e,n){t.exports={"default":n(6),__esModule:!0}},function(t,e,n){n(7);var r=n(10).Object;t.exports=function(t,e,n){return r.defineProperty(t,e,n)}},function(t,e,n){var r=n(8);r(r.S+r.F*!n(18),"Object",{defineProperty:n(14).f})},function(t,e,n){var r=n(9),o=n(10),i=n(11),a=n(13),u="prototype",s=function(t,e,n){var c,l,f,d=t&s.F,v=t&s.G,p=t&s.S,h=t&s.P,m=t&s.B,y=t&s.W,_=v?o:o[e]||(o[e]={}),g=_[u],$=v?r:p?r[e]:(r[e]||{})[u];v&&(n=e);for(c in n)l=!d&&$&&void 0!==$[c],l&&c in _||(f=l?$[c]:n[c],_[c]=v&&"function"!=typeof $[c]?n[c]:m&&l?i(f,r):y&&$[c]==f?function(t){var e=function(e,n,r){if(this instanceof t){switch(arguments.length){case 0:return new t;case 1:return new t(e);case 2:return new t(e,n)}return new t(e,n,r)}return t.apply(this,arguments)};return e[u]=t[u],e}(f):h&&"function"==typeof f?i(Function.call,f):f,h&&((_.virtual||(_.virtual={}))[c]=f,t&s.R&&g&&!g[c]&&a(g,c,f)))};s.F=1,s.G=2,s.S=4,s.P=8,s.B=16,s.W=32,s.U=64,s.R=128,t.exports=s},function(t,e){var n=t.exports="undefined"!=typeof window&&window.Math==Math?window:"undefined"!=typeof self&&self.Math==Math?self:Function("return this")();"number"==typeof __g&&(__g=n)},function(t,e){var n=t.exports={version:"2.2.0"};"number"==typeof __e&&(__e=n)},function(t,e,n){var r=n(12);t.exports=function(t,e,n){if(r(t),void 0===e)return t;switch(n){case 1:return function(n){return t.call(e,n)};case 2:return function(n,r){return t.call(e,n,r)};case 3:return function(n,r,o){return t.call(e,n,r,o)}}return function(){return t.apply(e,arguments)}}},function(t,e){t.exports=function(t){if("function"!=typeof t)throw TypeError(t+" is not a function!");return t}},function(t,e,n){var r=n(14),o=n(22);t.exports=n(18)?function(t,e,n){return r.f(t,e,o(1,n))}:function(t,e,n){return t[e]=n,t}},function(t,e,n){var r=n(15),o=n(17),i=n(21),a=Object.defineProperty;e.f=n(18)?Object.defineProperty:function(t,e,n){if(r(t),e=i(e,!0),r(n),o)try{return a(t,e,n)}catch(u){}if("get"in n||"set"in n)throw TypeError("Accessors not supported!");return"value"in n&&(t[e]=n.value),t}},function(t,e,n){var r=n(16);t.exports=function(t){if(!r(t))throw TypeError(t+" is not an object!");return t}},function(t,e){t.exports=function(t){return"object"==typeof t?null!==t:"function"==typeof t}},function(t,e,n){t.exports=!n(18)&&!n(19)(function(){return 7!=Object.defineProperty(n(20)("div"),"a",{get:function(){return 7}}).a})},function(t,e,n){t.exports=!n(19)(function(){return 7!=Object.defineProperty({},"a",{get:function(){return 7}}).a})},function(t,e){t.exports=function(t){try{return!!t()}catch(e){return!0}}},function(t,e,n){var r=n(16),o=n(9).document,i=r(o)&&r(o.createElement);t.exports=function(t){return i?o.createElement(t):{}}},function(t,e,n){var r=n(16);t.exports=function(t,e){if(!r(t))return t;var n,o;if(e&&"function"==typeof(n=t.toString)&&!r(o=n.call(t)))return o;if("function"==typeof(n=t.valueOf)&&!r(o=n.call(t)))return o;if(!e&&"function"==typeof(n=t.toString)&&!r(o=n.call(t)))return o;throw TypeError("Can't convert object to primitive value")}},function(t,e){t.exports=function(t,e){return{enumerable:!(1&t),configurable:!(2&t),writable:!(4&t),value:e}}},function(t,e,n){"use strict";function r(t){return t&&t.__esModule?t:{"default":t}}Object.defineProperty(e,"__esModule",{value:!0});var o=n(3),i=r(o),a=n(4),u=r(a),s=function(){function t(e){(0,i["default"])(this,t),this.$inputs=e,this.arrErrors=[],this._assignEvents()}return(0,u["default"])(t,[{key:"_assignEvents",value:function(){var t=this;this.$inputs.on("input",function(e){var n=$(e.currentTarget);t._validateImmediate(n),t._removeError(n)})}},{key:"_validateImmediate",value:function(t){t.hasClass("type-numeric")&&t.val(t.val().replace(/[^\d]+/g,""))}},{key:"isValidInputs",value:function(){var t=this,e=this.$inputs,n=0;return e.each(function(e,r){var o=$(r);t._isValidInput(o)||(n+=1)}),Boolean(!n)}},{key:"_isValidInput",value:function(t){var e=$.trim(t.val());return e||t.hasClass("type-optional")?t.hasClass("type-email")&&!this._isValidEmail(e)?(this._setError(t,"Email is not valid"),!1):!0:(this._setError(t,"Empty"),!1)}},{key:"_isValidEmail",value:function(t){var e=/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;return e.test(t)}},{key:"_setError",value:function(t,e){var n=t.parent(),r=n.find(".b-error");r.length||(n.addClass("b-error_show"),$('<div class="b-error" />').text(e).appendTo(n),this.arrErrors.push({name:t.attr("name"),error:e}))}},{key:"_removeError",value:function(t){var e=t.parent();e.removeClass("b-error_show").find(".b-error").remove(),this.arrErrors=this.arrErrors.filter(function(e){return e.name!==t.attr("name")})}},{key:"setErrors",value:function(t){var e=this;t.forEach(function(t){var n=e.$inputs.filter('[name="'+t.name+'"]').first();n.length&&e._setError(n,t.error)})}},{key:"getErrorsText",value:function(t){var e=t||this.arrErrors,n="";return e.forEach(function(t){var e=t.name[0].toUpperCase()+t.name.substr(1);n+=e+": "+t.error+". "}),n}},{key:"getErrorsFull",value:function(t){var e=t||this.arrErrors,n=$("body"),r="";return e.forEach(function(t){var e=n.find('input[name="'+t.name+'"]').first(),o=e.length?e.attr("title"):t.name;r+="<b>"+o+"</b>: "+t.error+".  <br><br>"}),r}},{key:"getFormData",value:function(){var t={};return this.$inputs.map(function(e,n){var r=$(n),o=r.attr("name");o&&(t[o]=r.val())}),t}},{key:"removeErrors",value:function(){var t=this;this.$inputs.each(function(e,n){var r=$(n);t._removeError(r)})}},{key:"getFormData",value:function(){var t={};return this.$inputs.map(function(e,n){var r=$(n),o=r.attr("name");o&&(t[o]=r.val())}),t}},{key:"clearForm",value:function(){this.$inputs.each(function(t,e){var n=$(e);n.attr("disabled")||n.val("")})}}]),t}();e["default"]=s},,,,,,,,,,,,,,,,,function(t,e,n){"use strict";function r(t){return t&&t.__esModule?t:{"default":t}}var o=n(41),i=r(o),a=n(42),u=r(a);$(function(){u["default"].plugin(".js-social-connect"),i["default"].plugin("#dlg-email-connect",{url:jsRoutes.controllers.core.UserAccounts.handleNewPassword().url}).on("hmt.emailconnect.success",function(t){$(".js-email-connect").addClass("show_connected"),success("You created new email account")}),i["default"].plugin("#dlg-change-password",{url:jsRoutes.controllers.core.UserAccounts.changePassword().url}).on("hmt.emailconnect.success",function(t){var e="Your password was successfully updated";success(e)}),i["default"].plugin("#dlg-change-email",{url:jsRoutes.controllers.core.UserAccounts.changeEmail().url}).on("hmt.emailconnect.success",function(t){var e="Please check your mailbox and click a confirmation link to complete an email change process";success(e)})})},function(t,e,n){"use strict";function r(t){return t&&t.__esModule?t:{"default":t}}Object.defineProperty(e,"__esModule",{value:!0});var o=n(3),i=r(o),a=n(4),u=r(a),s=n(23),c=r(s),l=function(){function t(e,n){(0,i["default"])(this,t),this.$root=$(e),this.locals=this._getDom(),this.options=$.extend({},n,this.$root.data()),this.validation=new c["default"](this.locals.$inputs),this._assignEvents()}return(0,u["default"])(t,[{key:"_getDom",value:function(){var t=this.$root;return{$form:t.find("form"),$inputs:t.find("form input")}}},{key:"_assignEvents",value:function(){this.locals.$form.on("submit",this._onSubmitForm.bind(this)),this.$root.on("hide.bs.modal",this._onHideModal.bind(this))}},{key:"_onSubmitForm",value:function(t){var e=this;if(t.preventDefault(),e.validation.isValidInputs()){var n=e.validation.getFormData();e._sendData(n).done(function(){e.$root.modal("hide"),e.validation.clearForm(),e.$root.trigger("hmt.emailconnect.success")}).fail(function(t){var n=$.parseJSON(t.responseText).data;n.errors&&e.validation.setErrors(n.errors)})}}},{key:"_onHideModal",value:function(){this.validation.clearForm(),this.validation.removeErrors()}},{key:"_sendData",value:function(t){return $.post(this.options.url,t)}}],[{key:"plugin",value:function(e,n){var r=$(e);if(r.length)return r.each(function(e,r){var o=$(r),i=o.data("widget.scrollto");i||(i=new t(r,n),o.data("widget",i))})}}]),t}();e["default"]=l},function(t,e,n){"use strict";function r(t){return t&&t.__esModule?t:{"default":t}}Object.defineProperty(e,"__esModule",{value:!0});var o=n(3),i=r(o),a=n(4),u=r(a),s=n(23),c=(r(s),function(){function t(e,n){(0,i["default"])(this,t),this.$root=$(e),this.options=$.extend({},n,this.$root.data()),this._assignEvents()}return(0,u["default"])(t,[{key:"_assignEvents",value:function(){this.$root.on("click","[data-social-connect]",this._onClickConnect.bind(this))}},{key:"_onClickConnect",value:function(t){t.preventDefault();var e=$(t.currentTarget).closest(".b-connect-i");this.toggleConnect(e)}},{key:"_setConnect",value:function(t){window.location=t.data("url")}},{key:"_unSetConnect",value:function(t){var e=t.data("social"),n=jsRoutes.controllers.core.UserAccounts.disconnect(e).url;$.post(n,{},function(e){t.removeClass("state-complete"),success(e.message)},"json").fail(function(t,e,n){var r=JSON.parse(t.responseText);error(r.message)})}},{key:"toggleConnect",value:function(t){t.hasClass("state-complete")?this._unSetConnect(t):this._setConnect(t)}}],[{key:"plugin",value:function(e,n){var r=$(e);if(r.length)return r.each(function(e,r){var o=$(r),i=o.data("widget.scrollto");i||(i=new t(r,n),o.data("widget",i))})}}]),t}());e["default"]=c}]);