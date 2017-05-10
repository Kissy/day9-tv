$(document).ready(function(){

/*
----------------------------------------------------------------
	Replacements...
---------------------------------------------------------------- */

        //Cufon.set('fontFamily', 'Tungsten Medium').replace('h1')('h2');

	Cufon.replace('.back', {fontFamily: 'Tungsten Medium', hover: true});
	Cufon.replace('h1', {fontFamily: 'Tungsten Medium', hover: true});
	Cufon.replace('h2', {fontFamily: 'Tungsten Medium', hover: true});
	Cufon.replace('h3', {fontFamily: 'Tungsten Medium', hover: true});
	Cufon.replace('h4', {fontFamily: 'Tungsten Medium', hover: true});
	Cufon.replace('h5', {fontFamily: 'Tungsten Medium', hover: true});

	//Cufon.replace('#countdown .counter', { fontFamily: 'Tungsten Medium', hover: true });
	Cufon.replace('#nav a', {fontFamily: 'Tungsten Medium', hover: true});

/*
----------------------------------------------------------------
	COMMON
---------------------------------------------------------------- */

	$("input").clearDefault();
	$("textarea").clearDefault();

/*
----------------------------------------------------------------
	AJAX
---------------------------------------------------------------- */

        if ($.browser.mozilla) {
            $('.header-right').css('top', '100px');
        }



        $('#live').click(function() {
            var live_url = $('p.url', this).html();
            window.location = live_url;
        })


        //$('.tag-input').val('');

        $('.ajax-link').live('click', function(e) {
            e.preventDefault();
            $('.ajax-link').removeClass('active');
            $(this).addClass('active');
            $('table.vods-list tbody').html('');
            $('#loading').show();
            var category_id = $(this).attr('id').substring(16);
            var request_url = '';
            if (category_id == 0)
                request_url =  url + '/ajax/?cmd=get-videos';
            else
                request_url =  url + '/ajax/?cmd=get-videos&category=' + category_id;

            var s = $('.tag-input').val();
            if (s)
                request_url += '&search=' + s;

            request_videos(request_url);
        });

        $('.ajax-tag').live('click', function(e) {
            e.preventDefault();
            $('table.vods-list tbody').html('');
            $('#loading').show();
            var category_id = $('.ajax-link').filter('.active').attr('id').substring(16);
            var request_url = '';
            if (category_id == 0)
                request_url =  url + '/ajax/?cmd=get-videos';
            else
                request_url =  url + '/ajax/?cmd=get-videos&category=' + category_id;

            var tag = $(this).html();
            $('.tag-input').val(tag);
            request_url += '&search=' + tag;
            request_videos(request_url);
        });

        $('.tag-input').keyup(function() {
            var s = $(this).val();
            var category_id = $('.ajax-link').filter('.active').attr('id').substring(16);
            var request_url = '';
            delay(function(){
                if (category_id == 0)
                    request_url =  url + '/ajax/?cmd=get-videos';
                else
                    request_url =  url + '/ajax/?cmd=get-videos&category=' + category_id;

                //if (s.length >= 3) {
                    $('table.vods-list tbody').html('');
                    $('#loading').show();

                    request_url += '&search=' + s;
                    request_videos(request_url);

                //}
            }, 500 );
        });

        function request_videos(request_url) {
            // Example url:
            //   http://day9.tv.staging.pokkari.net/ajax/?cmd=get-videos&category=daylies

            $.getJSON(request_url, function(data) {
                var html = '';
                if (data.length == 0) {
                    html += '<tr class="">';
                    html += '<td conspan="3" style="text-align: center;">No video match with your search</td>';
                    html += '</tr>';
                }
                else {
                    $.each(data, function(key, value) {
                        html += '<tr class="">';
                        html += '<td class="archive-title-video"><a href="' + value['url'] + '" target="_blank" title="' + value['title'] + '">' + value['title'] + '</a></td>';
                        html += '<td class="comments"><span><a href="' + value['url'] + '#disqus_thread" target="_blank">0</a></span></td>';
                        html += '<td class="tags">' + value['tags'] + '</td>';
                        html += '</tr>';
                    });
                }

                $('#loading').hide();
                $('table.vods-list tbody').html(html);
            })
        }


/*
----------------------------------------------------------------
	HOMEPAGE
---------------------------------------------------------------- */
	/*$(".live").click(function(){
		$(".live-stream").show("fast");
	});
	*/
	var infoVisible = false;
	$(".home-calendar").hover(
		function(){
			$(".month li").hover(
				function(){
					$(this).children("a").stop(false, true).animate({"color": "#ffa719"},100);
					if (infoVisible == false){
						$(".event-info").stop(false, true).fadeIn(150);
					} else  {
						$(".event-info").text($(this).children("a").text());
						$(".event-info").css("top",$(this).children("a").position().top);

					}
					infoVisible = true;
				},
				function(){
					if($(this).hasClass("featured")){
						$(this).children("a").stop(false, true).animate({"color": "#ff3333"},250);
					}
					else  $(this).children("a").stop(false, true).animate({"color": "#62605d"},250);

					bInfo = false;
				}
			)
		},
		function(){
			$(".event-info").stop(false, true).fadeOut(250);
			infoVisible = false;
		}
	);

/*
----------------------------------------------------------------
	HEADER
---------------------------------------------------------------- */
	//var date = new Date();
	//var hours = 12;
	//var mins = 34;
	//var segs = 45;
	//clog(date.getSeconds());
	//$("#countdown .counter").text(hours+":"+mins+":"+segs);

	$("#countdown .what").hover(
		function(){
			$("#countdown .info").stop(false, true).fadeIn(200);
			$(this).css("opacity","0.7");
		},
		function(){
			$("#countdown .info").stop(false, true).fadeOut(300);
			$(this).css("opacity","1");
		}
	);
	$("#logo a").hover(
		function(){
			$(this).animate({"opacity":"0.7"},200);
		},
		function(){
			$(this).animate({"opacity":"1"},300);
		}
	);
/*
----------------------------------------------------------------
	SINGLE VIDEO
---------------------------------------------------------------- */
var isBig = false;
$(".video-player").hover(
	function(){
		if (isBig == true) $(this).css("height","559px");
		else $(this).css("height","412");

	},
	function(){
		if (isBig == true) $(this).css("height","528");
		else $(this).css("height","382");
	}
)

$(".lights-off").toggle(
	function(){

			isBig = true;
			$("body").children("div").each(function(){
				var idTag = $(this).attr("id");
				//console.log(idTag);
				if(idTag !== "content"){
					$(this).animate({"opacity":"0.1"},{queue: false, duration:650});
				}
			});
			$(".back").animate({"opacity":"0.1"},{queue: false, duration:650});
			$(".title").animate({"opacity":"0.1"},{queue: false, duration:650});
			$(".comments-block").animate({"opacity":"0.1"},{queue: false, duration:650});

			$(".video").animate({"height":"534"},{queue: false, duration:650});
			$(".video-player-footer").animate({"top":"632"},{queue: false, duration:650});
			$(".video-player-footer").animate({"width":"940"},{queue: false, duration:650});
			$(".video-player").animate({"width":"940"},{queue: false, duration:650});
			$(".video-player").animate({"height":"529"},{queue: false, duration:650});
			$(".video-embed").animate({"width":"940"},{queue: false, duration:650});
			$(".video-embed").animate({"height":"559"},{queue: false, duration:650});
			$(".about-lights").animate({"opacity":"0.25"},{queue: false, duration:650});
			$(".about-likit").animate({"opacity":"0.25"},{queue: false, duration:650});
			$("body").css("backgroundImage","none");
			$("body").animate({"backgroundColor":"#000000"},{queue: false, duration:650});
	},
	function(){
		isBig = false;
		$("body").children("div").each(function(){
			var idTag = $(this).attr("id");
			if(idTag !== "content"){
				$(this).animate({"opacity":"1"},{queue: false, duration:650});
			}
		});
		$(".back").animate({"opacity":"1"},{queue: false, duration:650});
		$(".title").animate({"opacity":"1"},{queue: false, duration:650});
		$(".comments-block").animate({"opacity":"1"},{queue: false, duration:650});

		$(".video").animate({"height":"412"},{queue: false, duration:650});
		$(".video-player-footer").animate({"top":"486"},{queue: false, duration:650});
		$(".video-player-footer").animate({"width":"677"},{queue: false, duration:650});
		$(".video-player").animate({"width":"678"},{queue: false, duration:650});
		$(".video-player").animate({"height":"382"},{queue: false, duration:650});
		$(".video-embed").animate({"width":"678"},{queue: false, duration:650});
		$(".video-embed").animate({"height":"412"},{queue: false, duration:650});
		$(".about-lights").animate({"opacity":"1"},{queue: false, duration:650});
		$(".about-likit").animate({"opacity":"1"},{queue: false, duration:650});
		$("body").css("backgroundImage","url('../images/bkg.jpg')");
		$("body").animate({"backgroundColor":"#131213"},{queue: false, duration:650});
	}
);

$('.comments-block textarea').autoResize({
    // Quite slow animation:
    animateDuration : 350,
    // More extra space:
    extraSpace : 30
});

$(".getplaylist a").click(function(){
		var post_id = $(this).attr("rel")
		$("#loading-playlist").html("loading...");
		$("#loading-playlist").load("/getplaylist/?cmd=" + post_id,function() { Cufon.replace('#loading-playlist *') });
		return false;
	});


/*
----------------------------------------------------------------
	ARCHIVE
---------------------------------------------------------------- */
	$(".video-archive .vods-list").hover(
		function(){
			$("tr").stop(false, true).animate({"background": "#ffffff"},100);
		},
		function(){
			$("tr").stop(false, true).animate({"background": "#000000"},250);
		}
	);


/*
----------------------------------------------------------------
	FOOTER
---------------------------------------------------------------- */
	//$(".footer-nav li:last-child").css("marginRight", "0px");
	$(".latest-vods li").hover(
		function(){
			$(this).children("a").stop(false, true).animate({"color": "#ffa71a"},100);
		},
		function(){
			$(this).children("a").stop(false, true).animate({"color": "#63605e"},250);
		}
	);


});//End Jquery Ready


(function($){
	$.fn.clearDefault = function(){
		return this.each(function(){
			var default_value = $(this).val();
			$(this).focus(function(){
				if ($(this).val() == default_value) $(this).val("");
			});
			$(this).blur(function(){
				if ($(this).val() == "") $(this).val(default_value);
			});
		});
	};
})(jQuery);
// Usage: $('input.clear-default').clearDefault();

var delay = (function(){
  var timer = 0;
  return function(callback, ms){
    clearTimeout (timer);
    timer = setTimeout(callback, ms);
  };
})();