'use strict';

import SetCredits from "./widgets/_set-credits";

$(function(){
    App.events
        .sub('hmt.tab.shown', function(){
            SetCredits.plugin('.js-set-credits');
        })
});