(function($) {
  "use strict";
  
	  // Configure tooltips for collapsed side navigation
	  $('.navbar-sidenav [data-toggle="tooltip"]').tooltip({
		template: '<div class="tooltip navbar-sidenav-tooltip" role="tooltip"><div class="arrow"></div><div class="tooltip-inner"></div></div>'
	  })
	  // Toggle the side navigation
	  $("#sidenavToggler").click(function(e) {
		e.preventDefault();
		$("body").toggleClass("sidenav-toggled");
		$(".navbar-sidenav .nav-link-collapse").addClass("collapsed");
		$(".navbar-sidenav .sidenav-second-level, .navbar-sidenav .sidenav-third-level").removeClass("show");
	  });
	  // Force the toggled class to be removed when a collapsible nav link is clicked
	  $(".navbar-sidenav .nav-link-collapse").click(function(e) {
		e.preventDefault();
		$("body").removeClass("sidenav-toggled");
	  });
	  
     // Prevent the content wrapper from scrolling when the fixed side navigation hovered over
	  $('body.fixed-nav .navbar-sidenav, body.fixed-nav .sidenav-toggler, body.fixed-nav .navbar-collapse').on('mousewheel DOMMouseScroll', function(e) {
		var e0 = e.originalEvent,
		  delta = e0.wheelDelta || -e0.detail;
		this.scrollTop += (delta < 0 ? 1 : -1) * 30;
		e.preventDefault();
	  });
  
	  // Scroll to top button appear
	  $(document).scroll(function() {
		var scrollDistance = $(this).scrollTop();
		if (scrollDistance > 100) {
		  $('.scroll-to-top').fadeIn();
		} else {
		  $('.scroll-to-top').fadeOut();
		}
	  });
	  // Configure tooltips globally
	  $('[data-toggle="tooltip"]').tooltip()
	  
	  // Smooth scrolling using jQuery easing
	  $(document).on('click', 'a.scroll-to-top', function(event) {
		var $anchor = $(this);
		$('html, body').stop().animate({
		  scrollTop: ($($anchor.attr('href')).offset().top)
		}, 1000, 'easeInOutExpo');
		event.preventDefault();
	  });
	  
	  // Slim Scroll message & notification
	  $('#message-box').slimScroll({
		color: '#f2f7fb',
		size: '5px',
		height: '250px',
		alwaysVisible: true
	  });
	  
	  $('#notification-box').slimScroll({
		color: '#f2f7fb',
		size: '5px',
		height: '250px',
		alwaysVisible: true
	  });
	  
	   $('.rightMenu-scroll').slimScroll({
			height: '650px',
			color: '#f4f5f7'
		});
	  
	  
	  // Twitter Slick Slider
	  $('.social-card-slide').slick({
	  dots:false,
	  speed:600,
	  autoplay: true,
	  slidesToShow: 1,
	  arrows: false, 
	});
	
	// cOLOR cHANGING 
	var a, i = ["red-skin", "blue-skin", "green-skin", "yellow-skin", "purple-skin", "cyan-skin", "red-skin-light", "blue-skin-light", "green-skin-light", "yellow-skin-light", "purple-skin-light", "cyan-skin-light"];

	function o(e) {
		var a, o;
		return $.each(i, function(e) {
			$("body").removeClass(i[e])
		}), $("body").addClass(e), a = "skin", o = e, "undefined" != typeof Storage ? localStorage.setItem(a, o) : window.alert("Please use a modern browser to properly view this template!"), !1
	}(a = void("undefined" != typeof Storage || window.alert("Please use a modern browser to properly view this template!"))) && $.inArray(a, i) && o(a), $("[data-skin]").on("click", function(e) {
		$(this).hasClass("knob") || (e.preventDefault(), o($(this).data("skin")))
	})
	  
})(jQuery); // End of use strict
