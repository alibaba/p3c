require(['gitbook', 'jQuery'], function (gitbook, $) {

	if (!window.history.pushState || !window.sessionStorage) return;

	var KEY_SCROLL_POSITION = 'book_summary_scroll_postion_saver';
	var $summary;

	bindChangePushState(function() {
		window.sessionStorage.setItem(KEY_SCROLL_POSITION, $summary.scrollTop())
	});

	gitbook.events.bind('page.change', function () {
		var savedScrollPosition
			= Number(window.sessionStorage.getItem(KEY_SCROLL_POSITION), 10) || 0;
		
		$summary = $('.book-summary .summary');

		window.setTimeout(function() {
			$summary.scrollTop(savedScrollPosition);
		}, 50);	
	});

	function bindChangePushState(cb) {
		var pushState = window.history.pushState;
		window.history.pushState = function (state) {
			cb();
			return pushState.apply(window.history, arguments);
		}
	}
});
