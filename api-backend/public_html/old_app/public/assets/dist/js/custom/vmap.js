(function($) {
  "use strict";
  
  // jvectormap data
  var visitorsData = {
    US: 398, // USA
    SA: 400, // Saudi Arabia
    CA: 1000, // Canada
    DE: 500, // Germany
    FR: 760, // France
    CN: 300, // China
    AU: 700, // Australia
    BR: 600, // Brazil
    IN: 800, // India
    GB: 320, // Great Britain
    RU: 3000 // Russia
  };

  // World map by jvectormap
  $('#world-map').vectorMap({
    map              : 'world_mill_en',
    backgroundColor  : 'transparent',
    regionStyle      : {
      initial: {
        fill            : '#dfe9ef',
        'fill-opacity'  : 1,
        stroke          : 'none',
        'stroke-width'  : 0,
        'stroke-opacity': 1
      }
    },
    series           : {
      regions: [
        {
          values           : visitorsData,
          scale            : ['#00afff', '#39c8ed'],
          normalizeFunction: 'polynomial'
        }
      ]
    },
    onRegionLabelShow: function (e, el, code) {
      if (typeof visitorsData[code] != 'undefined')
        el.html(el.html() + ': ' + visitorsData[code] + ' new visitors');
    }
  });
  
  $('#india-map').vectorMap({
	map : 'in_mill',
		backgroundColor : 'transparent',
		regionStyle : {
			initial : {
				fill : '#00dcbf'
			}
		}
	});
	
	$('#au-map').vectorMap({
		map : 'au_mill',
		backgroundColor : 'transparent',
		regionStyle : {
			initial : {
				fill : '#ff9800'
			}
		}
	});
	
	$('#usa-map').vectorMap({
		map : 'us_aea_en',
		backgroundColor : 'transparent',
		regionStyle : {
			initial : {
				fill : '#1cc100'
			}
		}
	});
	
	$('#uk-map').vectorMap({
		map : 'uk_mill_en',
		backgroundColor : 'transparent',
		regionStyle : {
			initial : {
				fill : '#ff3a72'
			}
		}
	});

  

  
  
	
})(jQuery); // End of use strict