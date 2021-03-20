import $ from 'jquery';
window.$ = $;

import 'jquery-ui';
import 'jquery-ui/ui/widgets/sortable';

import 'jquery-datetimepicker';
import 'jquery-datetimepicker/jquery.datetimepicker.css';

import 'jsrender';

import 'blueimp-file-upload';
import 'blueimp-file-upload/css/jquery.fileupload.css';

//import 'bootstrap';
import 'bootstrap/dist/js/bootstrap.bundle.min';
import 'bootstrap/dist/css/bootstrap.min.css';

import feather from 'feather-icons';
window.feather = feather;

// import 'froala-editor';
import FroalaEditor from 'froala-editor';
// 將 FroalaEditor 加到 window 下，這樣可以在 JS 中全局可見。
window.FroalaEditor = FroalaEditor; 

import 'froala-editor/js/plugins/align.min';
import 'froala-editor/js/plugins/char_counter.min';
import 'froala-editor/js/plugins/code_beautifier.min';
import 'froala-editor/js/plugins/code_view.min';

import 'froala-editor/js/plugins/colors.min';
import 'froala-editor/js/plugins/draggable.min';
import 'froala-editor/js/plugins/emoticons.min';
import 'froala-editor/js/plugins/entities.min';
import 'froala-editor/js/plugins/file.min';
import 'froala-editor/js/plugins/font_family.min';
import 'froala-editor/js/plugins/font_size.min';
import 'froala-editor/js/plugins/fullscreen.min';
import 'froala-editor/js/plugins/image.min';
import 'froala-editor/js/plugins/image_manager.min';
import 'froala-editor/js/plugins/inline_style.min';
import 'froala-editor/js/plugins/line_breaker.min';
import 'froala-editor/js/plugins/link.min';
import 'froala-editor/js/plugins/lists.min';
import 'froala-editor/js/plugins/paragraph_format.min';
import 'froala-editor/js/plugins/paragraph_style.min';
import 'froala-editor/js/plugins/quick_insert.min';
import 'froala-editor/js/plugins/quote.min';
import 'froala-editor/js/plugins/table.min';
import 'froala-editor/js/plugins/save.min';
import 'froala-editor/js/plugins/url.min';
import 'froala-editor/js/plugins/video.min';
import 'froala-editor/js/plugins/word_paste.min';
//import 'froala-editor/js/third_party/image_aviary.min';
import 'froala-editor/js/third_party/image_tui.min';
import 'froala-editor/css/third_party/image_tui.min.css';

import 'froala-editor/css/froala_editor.css';
import 'froala-editor/css/froala_style.css';
import 'froala-editor/css/plugins/char_counter.css';
import 'froala-editor/css/plugins/code_view.css';
import 'froala-editor/css/plugins/colors.css';
import 'froala-editor/css/plugins/draggable.css';
import 'froala-editor/css/plugins/emoticons.css';
import 'froala-editor/css/plugins/file.css';
import 'froala-editor/css/plugins/fullscreen.css';
import 'froala-editor/css/plugins/image.css';
import 'froala-editor/css/plugins/image_manager.css';
import 'froala-editor/css/plugins/line_breaker.css';
import 'froala-editor/css/plugins/quick_insert.css';
import 'froala-editor/css/plugins/table.css';
import 'froala-editor/css/plugins/video.css';

import 'froala-editor/js/languages/zh_cn.js';
import 'froala-editor/js/languages/zh_tw.js';
import 'froala-editor/js/languages/ja.js';

// import moment from 'moment';
// window.moment = moment;
import { format as date_fns_format, parse as date_fns_parse }  from 'date-fns';
window.date_fns_format = date_fns_format;
window.date_fns_parse = date_fns_parse;


import 'select2';
import 'select2/select2.css';

//import PNotify from 'pnotify';
//import 'pnotify/dist/pnotify.css';
//PNotify.prototype.options.styling = "bootstrap4";
import * as PNotify from '@pnotify/core';
import '@pnotify/core/dist/PNotify.css';
import '@pnotify/core/dist/BrightTheme.css';
import PNotifyBootstrap4 from '@pnotify/bootstrap4';
import '@pnotify/bootstrap4/dist/PNotifyBootstrap4.css';

PNotify.defaultModules.set(PNotifyBootstrap4, {});
PNotify.styling = "BrightTheme";
window.PNotify = PNotify;

import './resources/js/jquery.shiftcheckbox';
import './resources/js/jquery.mjs.nestedSortable';
import './resources/js/common'

import './resources/css/flaticon.css';
import './resources/css/wallride.css';

import 'setimmediate'; 

import { logger } from './resources/js/logger';
window.logger = logger; 
//logger.info('I will be logged in all-the-logs.log');