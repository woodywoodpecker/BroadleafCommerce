/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
;(function($, window, undefined) {
    'use strict';

    $.fn.broadleafTabs = function(options) {

        var initTabs = function() {

            // Only show the 'active' tab
            $.each($('.entityFormTab'), function(key, value) {
                if ($(value).hasClass('active')) {
                    $(value).show();
                } else {
                    $(value).hide();
                }
            });
        };

        $(document).on('click', '.nav-tabs>li>a', function(e) {
            e.preventDefault();

            // Remove 'selected' outline from tab
            $(this).blur();

            // Remove 'active' class from all tabs
            $.each($('.nav-tabs>li'), function (key, value) {
                $(value).removeClass('active');
            });

            // Add 'active' class to current tab
            $(this).parent().addClass('active');

            // Remove 'active' class from all tab content
            $.each($('.entityFormTab'), function (key, value) {
                $(value).removeClass('active');
            });

            // Add 'active' class to current tab content
            var tab = this.href.substring(this.href.indexOf("#") + 1);
            tab = '.' + tab + 'Tab';
            $(tab).addClass('active');

            // Show or hide tab content based on 'active' class
            $.each($('.entityFormTab'), function(key, value) {
                if ($(value).hasClass('active')) {
                    $(value).show();
                } else {
                    $(value).hide();
                }
            });

            BLCAdmin.initializeFields(BLCAdmin.getActiveTab());
            BLCAdmin.updateFields(BLCAdmin.getActiveTab());
        });

        initTabs();
    };

})(jQuery, this);
