$(document).ready(function () {
    $(".sideBarli").removeClass("activeLi");
    $(".contentSideA").addClass("activeLi");

    function convertMinutesToHoursAndMinutes(minutes) {
        const hours = Math.floor(minutes / 60);
        const remainingMinutes = minutes % 60;
        return `${hours} h ${remainingMinutes} m`;
    }
    let currentMoviePage = 1;
    let totalMoviePages = 1;
    let currentSeriesPage = 1;
    let totalSeriesPages = 1;

    const maxVisiblePages = 5;
    $(".pagination-controls").hide();

    function updateMoviePaginationControls() {
        $("#prev-page-movie").prop("disabled", currentMoviePage === 1);
        $("#next-page-movie").prop(
            "disabled",
            currentMoviePage === totalMoviePages
        );

        let pageButtonsHtml = "";

        if (totalMoviePages <= maxVisiblePages) {
            for (let i = 1; i <= totalMoviePages; i++) {
                pageButtonsHtml += `<button class="pagination-ui-movie" data-page="${i}">${i}</button>`;
            }
        } else {
            if (currentMoviePage <= Math.ceil(maxVisiblePages / 2)) {
                for (let i = 1; i <= maxVisiblePages - 1; i++) {
                    pageButtonsHtml += `<button class="pagination-ui-movie" data-page="${i}">${i}</button>`;
                }
                pageButtonsHtml += `<span>...</span><button class="pagination-ui-movie" data-page="${totalMoviePages}">${totalMoviePages}</button>`;
            } else if (
                currentMoviePage >=
                totalMoviePages - Math.floor(maxVisiblePages / 2)
            ) {
                pageButtonsHtml += `<button class="pagination-ui-movie" data-page="1">1</button><span>...</span>`;
                for (
                    let i = totalMoviePages - (maxVisiblePages - 2);
                    i <= totalMoviePages;
                    i++
                ) {
                    pageButtonsHtml += `<button class="pagination-ui-movie" data-page="${i}">${i}</button>`;
                }
            } else {
                pageButtonsHtml += `<button class="pagination-ui-movie" data-page="1">1</button><span>...</span>`;
                for (
                    let i =
                        currentMoviePage -
                        Math.floor((maxVisiblePages - 2) / 2);
                    i <=
                    currentMoviePage + Math.floor((maxVisiblePages - 2) / 2);
                    i++
                ) {
                    pageButtonsHtml += `<button class="pagination-ui-movie" data-page="${i}">${i}</button>`;
                }
                pageButtonsHtml += `<span>...</span><button class="pagination-ui-movie" data-page="${totalMoviePages}">${totalMoviePages}</button>`;
            }
        }

        $("#movie-page-buttons").html(pageButtonsHtml);

        $(".pagination-ui-movie").removeClass("active");
        $(`.pagination-ui-movie[data-page=${currentMoviePage}]`).addClass(
            "active"
        );
    }

    function fetchMovies(page) {
        let movieQuery = $("#movieQuery").val().trim();
        if (!movieQuery) return;
       
        const tmdbURL = `https://api.themoviedb.org/3/search/movie?query=${encodeURIComponent(movieQuery)}&language=en-US&page=${page}&api_key=${TMDB_api_key}`;
        const movieQueryURL = `assets/proxy/tmdb_proxy.php?url=${encodeURIComponent(tmdbURL)}`;

        $.ajax({
            url: movieQueryURL,
            success: function (response) {
                if (typeof response === "string") {
                    try {
                        response = JSON.parse(response);
                    } catch (e) {
                        console.error("Failed to parse JSON:", e);
                        return;
                    }
                }

                totalMoviePages = response.total_pages || 1;
                currentMoviePage = response.page || 1;

                let items = response.results;
                if (items && items.length > 0) {
                    let data = items.map((item) => {
                        let verticalPosterPath = item.poster_path
                            ? `https://image.tmdb.org/t/p/w500${item.poster_path}`
                            : "assets/img/placeholder-image.png";
                        let horizontalPosterPath = item.backdrop_path
                            ? `https://image.tmdb.org/t/p/w500${item.backdrop_path}`
                            : "assets/img/placeholder-image.png";

                        return [
                            `<div class="tbl-posters">
                            <img src="${verticalPosterPath}" alt="${item.title}" class="tbl-vertical-poster" data-fancybox>
                            <img src="${horizontalPosterPath}" alt="${item.title}" class="tbl-horizontal-poster" data-fancybox>
                        </div>`,
                            `<div class="result-tbl-title">
                            <span class="content-title mb-2">${item.title}</span>
                            <span class="content-title">${item.release_date}</span>
                        </div>`,
                            `<div class="itemDescription result-tbl-description w-100">${item.overview}</div>`,
                            `<a href="javascript:;" class="btn btn-primary importContentData" rel="${item.id}">View Details</a>`,
                        ];
                    });

                    $("#resultOfMovieTable").DataTable({
                        destroy: true,
                        data: data,
                        columns: [
                            { title: "Poster" },
                            { title: "Title" },
                            { title: "Overview" },
                            { title: "Action" },
                        ],
                        paging: false,
                        pageLength: 10,
                        lengthChange: false,
                        searching: false,
                        info: false,
                        autoWidth: false,
                        responsive: false,
                        dom: "lrtip",
                        drawCallback: function () {
                            $("html, body").animate(
                                { scrollTop: $(".main-content").offset().top },
                                "slow"
                            );
                        },
                    });

                    $(".pagination-movie").show();
                } else {
                    $("#resultOfMovieTable")
                        .DataTable({
                            info: false,
                            paging: false,
                            lengthChange: false,
                            searching: false,
                        })
                        .clear()
                        .draw();
                }

                updateMoviePaginationControls();
            },
            error: function (response) {
                console.log("Error fetching movie data:", response);
            },
        });
    }

    fetchMovies(currentMoviePage);

    $("#prev-page-movie").on("click", function () {
        if (currentMoviePage > 1) {
            currentMoviePage--;
            fetchMovies(currentMoviePage);
        }
    });

    $("#next-page-movie").on("click", function () {
        if (currentMoviePage < totalMoviePages) {
            currentMoviePage++;
            fetchMovies(currentMoviePage);
        }
    });

    $("#movie-page-buttons").on("click", ".pagination-ui-movie", function () {
        currentMoviePage = $(this).data("page");
        fetchMovies(currentMoviePage);
    });

    $(document).on("submit", "#searchMovieTMDBForm", function (e) {
        e.preventDefault();
        currentMoviePage = 1;
        fetchMovies(currentMoviePage);
    });

    $(document).on("click", ".importContentData", function (e) {
        e.preventDefault();

        $(".saveButton").removeClass("spinning disabled");
        $(this).addClass("spinning disabled");


        let movieID = $(this).attr("rel");

        const tmdbURL = `https://api.themoviedb.org/3/movie/${movieID}?api_key=${TMDB_api_key}`
        const movieDetailURL = `assets/proxy/tmdb_proxy.php?url=${encodeURIComponent(tmdbURL)}`;

        $.ajax({
            url: movieDetailURL,
            type: "GET",
            success: function (response) {
                if (typeof response === "string") {
                    try {
                        response = JSON.parse(response);
                    } catch (e) {
                        console.error("Failed to parse JSON:", e);
                        return;
                    }
                }

                if (response) {
                    let verticalPosterPath = response.poster_path
                        ? `https://image.tmdb.org/t/p/w500${response.poster_path}`
                        : "assets/img/placeholder-image.png";

                    let horizontalPosterPath = response.backdrop_path
                        ? `https://image.tmdb.org/t/p/w500${response.backdrop_path}`
                        : "assets/img/placeholder-image.png";

                    $("#set_vertical_poster").attr("src", verticalPosterPath);
                    $("#set_horizontal_poster").attr(
                        "src",
                        horizontalPosterPath
                    );

                    $("#vertical_poster_url").val(verticalPosterPath);
                    $("#horizontal_poster_url").val(horizontalPosterPath);

                    let releaseYear = response.release_date
                        ? response.release_date.split("-")[0]
                        : "";

                    $("#set_title").val(response.title);
                    $("#set_description").val(response.overview);
                    $("#set_release_year").val(releaseYear);

                    let runtimeFormatted = convertMinutesToHoursAndMinutes(
                        response.runtime
                    );
                    $("#set_duration").val(runtimeFormatted);

                    let ratingsValue = response.vote_average
                        ? parseFloat(response.vote_average).toFixed(1)
                        : "";

                    $("#set_ratings").val(ratingsValue);
                    if (response.genres && response.genres.length > 0) {
                        let genreNames = response.genres.map(
                            (genre) => genre.name
                        );
                        let genreNamesString = genreNames.join(", ");

                        $.ajax({
                            type: "POST",
                            url: `${domainUrl}addGenre`,
                            data: { genres: genreNamesString },
                            success: function (genreResponse) {
                                if (genreResponse.status) {
                                    $("#set_selectGenre").empty();
                                    genreResponse.allGenres.forEach(function (
                                        genre
                                    ) {
                                        $("#set_selectGenre").append(
                                            new Option(genre.title, genre.id)
                                        );
                                    });
                                    let genreArray = genreResponse.data.map(
                                        (genre) => genre.id
                                    );
                                    $("#set_selectGenre")
                                        .val(genreArray)
                                        .selectric("refresh");
                                    let originalLanguageCode =
                                        response.original_language;
                                    $.ajax({
                                        type: "POST",
                                        url: `${domainUrl}addLanguage`,
                                        data: {
                                            languageCode: originalLanguageCode,
                                        },
                                        success: function (languageResponse) {
                                            if (languageResponse.status) {
                                                $("#set_language_id").empty();
                                                languageResponse.allLanguages.forEach(
                                                    function (language) {
                                                        $(
                                                            "#set_language_id"
                                                        ).append(
                                                            new Option(
                                                                language.title,
                                                                language.id
                                                            )
                                                        );
                                                    }
                                                );
                                                let languageId =
                                                    languageResponse.data;
                                                $("#set_language_id")
                                                    .val(languageId)
                                                    .selectric("refresh");
                                                $(".saveButton").removeClass(
                                                    "spinning disabled"
                                                );
                                                $(
                                                    ".importContentData"
                                                ).removeClass(
                                                    "spinning disabled"
                                                );
                                                $(
                                                    "#setMovieContentModal"
                                                ).modal("show");
                                            } else {
                                                console.log(
                                                    "Error adding language"
                                                );
                                            }
                                        },
                                        error: function (error) {
                                            console.error(
                                                "Error adding language:",
                                                error
                                            );
                                        },
                                    });
                                } else {
                                    console.log("Error adding genres");
                                }
                            },
                            error: function (error) {
                                console.error("Error adding genres:", error);
                            },
                        });
                    } else {
                        console.log("No genres found for this movie.");
                        $(".saveButton").removeClass("spinning disabled");
                        $(".importMovieData").removeClass("spinning disabled");
                        $("#setMovieContentModal").modal("show");
                    }
                } else {
                    console.log("Invalid response or no details found.");
                }
            },
            error: function (error) {
                console.error("Error fetching movie details:", error);
            },
        });
    });

    // Series

    function updateSeriesPaginationControls() {
        $("#prev-page-series").prop("disabled", currentSeriesPage === 1);
        $("#next-page-series").prop(
            "disabled",
            currentSeriesPage === totalSeriesPages
        );

        let pageButtonsHtml = "";

        if (totalSeriesPages <= maxVisiblePages) {
            for (let i = 1; i <= totalSeriesPages; i++) {
                pageButtonsHtml += `<button class="pagination-ui-series" data-page="${i}">${i}</button>`;
            }
        } else {
            if (currentSeriesPage <= Math.ceil(maxVisiblePages / 2)) {
                for (let i = 1; i <= maxVisiblePages - 1; i++) {
                    pageButtonsHtml += `<button class="pagination-ui-series" data-page="${i}">${i}</button>`;
                }
                pageButtonsHtml += `<span>...</span><button class="pagination-ui-series" data-page="${totalSeriesPages}">${totalSeriesPages}</button>`;
            } else if (
                currentSeriesPage >=
                totalSeriesPages - Math.floor(maxVisiblePages / 2)
            ) {
                pageButtonsHtml += `<button class="pagination-ui-series" data-page="1">1</button><span>...</span>`;
                for (
                    let i = totalSeriesPages - (maxVisiblePages - 2);
                    i <= totalSeriesPages;
                    i++
                ) {
                    pageButtonsHtml += `<button class="pagination-ui-series" data-page="${i}">${i}</button>`;
                }
            } else {
                pageButtonsHtml += `<button class="pagination-ui-series" data-page="1">1</button><span>...</span>`;
                for (
                    let i =
                        currentSeriesPage -
                        Math.floor((maxVisiblePages - 2) / 2);
                    i <=
                    currentSeriesPage + Math.floor((maxVisiblePages - 2) / 2);
                    i++
                ) {
                    pageButtonsHtml += `<button class="pagination-ui-series" data-page="${i}">${i}</button>`;
                }
                pageButtonsHtml += `<span>...</span><button class="pagination-ui-series" data-page="${totalSeriesPages}">${totalSeriesPages}</button>`;
            }
        }

        $("#series-page-buttons").html(pageButtonsHtml);

        $(".pagination-ui-series").removeClass("active");
        $(`.pagination-ui-series[data-page=${currentSeriesPage}]`).addClass(
            "active"
        );
    }

    function fetchSeries(page) {
        let seriesQuery = $("#seriesQuery").val().trim();
        if (!seriesQuery) return;
       
        const tmdbURL = `https://api.themoviedb.org/3/search/tv?query=${encodeURIComponent(seriesQuery)}&language=en-US&page=${page}&api_key=${TMDB_api_key}`;
        const seriesQueryURL = `assets/proxy/tmdb_proxy.php?url=${encodeURIComponent(tmdbURL)}`;

        $.ajax({
            url: seriesQueryURL,
            success: function (response, status, xhr) {
                if (typeof response === "string") {
                    try {
                        response = JSON.parse(response);
                    } catch (e) {
                        console.error("Failed to parse JSON:", e);
                        return;
                    }
                }

                let items = response.results;
                totalSeriesPages = response.total_pages;

                if (items.length > 0) {
                    let data = [];
                    $.each(items, function (index, item) {
                        let verticalPosterPath = item.poster_path
                            ? `https://image.tmdb.org/t/p/w500${item.poster_path}`
                            : "assets/img/placeholder-image.png";

                        let horizontalPosterPath = item.backdrop_path
                            ? `https://image.tmdb.org/t/p/w500${item.backdrop_path}`
                            : "assets/img/placeholder-image.png";

                        data.push([
                            `<div class="tbl-posters"><img src="${verticalPosterPath}" alt="${item.name}" class="tbl-vertical-poster" data-fancybox><img src="${horizontalPosterPath}" alt="${item.name}" class="tbl-horizontal-poster" data-fancybox></div>`,
                            `<div class="result-tbl-title"> <span class="content-title mb-2"> ${item.name} </span> <span class="content-title"> ${item.first_air_date} </span></div>`,
                            `<div class="itemDescription result-tbl-description w-100"> ${item.overview} </div>`,
                            `<a href="javascript:;" class="btn btn-primary importSeriesContentData" rel="${item.id}"> ${localization.importData} </a>`,
                        ]);
                    });

                    $("#resultOfSeriesTable").DataTable({
                        destroy: true,
                        data: data,
                        columns: [
                            { title: "Poster" },
                            { title: "Title" },
                            { title: "Overview" },
                            { title: "Action" },
                        ],
                        paging: false,
                        pageLength: 10,
                        lengthChange: false,
                        searching: false,
                        info: false,
                        autoWidth: false,
                        responsive: false,
                        dom: "lrtip",
                        drawCallback: function () {
                            $("html, body").animate(
                                {
                                    scrollTop: $(".main-content").offset().top,
                                },
                                "slow"
                            );
                        },
                    });

                    $(".pagination-series").show();
                } else {
                    $("#resultOfSeriesTable")
                    .DataTable({
                        info: false,
                        paging: false,
                        lengthChange: false,
                        searching: false,
                    })
                    .clear()
                    .draw();
                }

                updateSeriesPaginationControls();
            },
            error: function (response) {
                console.error("API request failed:", response);
            },
        });
    }

    fetchSeries(currentSeriesPage);

    $("#prev-page-series").on("click", function () {
        if (currentSeriesPage > 1) {
            currentSeriesPage--;
            fetchSeries(currentSeriesPage);
        }
    });

    $("#next-page-series").on("click", function () {
        if (currentSeriesPage < totalSeriesPages) {
            currentSeriesPage++;
            fetchSeries(currentSeriesPage);
        }
    });

    $(document).on("submit", "#searchSeriesTMDBForm", function (e) {
        e.preventDefault();
        currentSeriesPage = 1;
        fetchSeries(currentSeriesPage);
    });

    $("#series-page-buttons").on("click", ".pagination-ui-series", function () {
        currentSeriesPage = $(this).data("page");
        fetchSeries(currentSeriesPage);
    });

    $(document).on("submit", "#searchSeriesTMDBForm", function (e) {
        e.preventDefault();
        currentSeriesPage = 1;
        fetchSeries(currentSeriesPage);
    });

    $(document).on("click", ".importSeriesContentData", function (e) {
        e.preventDefault();

        // Disable the button and show spinner
        $(".saveButton").removeClass("spinning disabled");
        $(this).addClass("spinning disabled");

        let seriesID = $(this).attr("rel");

        const tmdbURL = `https://api.themoviedb.org/3/tv/${seriesID}?api_key=${TMDB_api_key}`
        const seriesDetailURL = `assets/proxy/tmdb_proxy.php?url=${encodeURIComponent(tmdbURL)}`;

        // Fetch series details from the server
        $.ajax({
            url: seriesDetailURL,
            type: "GET",
            success: function (response) {
                if (typeof response === "string") {
                    try {
                        response = JSON.parse(response);
                    } catch (e) {
                        console.error("Failed to parse JSON:", e);
                        return;
                    }
                }

                let verticalPosterPath = response.poster_path
                    ? `https://image.tmdb.org/t/p/w500${response.poster_path}`
                    : "assets/img/placeholder-image.png";

                let horizontalPosterPath = response.backdrop_path
                    ? `https://image.tmdb.org/t/p/w500${response.backdrop_path}`
                    : "assets/img/placeholder-image.png";

                $("#series_set_vertical_poster").attr(
                    "src",
                    verticalPosterPath
                );
                $("#series_set_horizontal_poster").attr(
                    "src",
                    horizontalPosterPath
                );

                $("#series_vertical_poster_url").val(verticalPosterPath);
                $("#series_horizontal_poster_url").val(horizontalPosterPath);

                let releaseYear = response.first_air_date
                    ? response.first_air_date.split("-")[0]
                    : "";

                $("#series_set_title").val(response.name);
                $("#series_set_description").val(response.overview);
                $("#series_set_release_year").val(releaseYear);

                let ratingsValue = response.vote_average
                    ? parseFloat(response.vote_average).toFixed(1)
                    : "";

                $("#series_set_ratings").val(ratingsValue);

                if (response.genres && response.genres.length > 0) {
                    let genreNames = response.genres.map((genre) => genre.name);
                    let genreNamesString = genreNames.join(", ");

                    $.ajax({
                        type: "POST",
                        url: `${domainUrl}addGenre`,
                        data: { genres: genreNamesString },
                        success: function (genreResponse) {
                            if (genreResponse.status) {
                                $("#series_set_selectGenre").empty();

                                genreResponse.allGenres.forEach(function (
                                    genre
                                ) {
                                    $("#series_set_selectGenre").append(
                                        new Option(genre.title, genre.id)
                                    );
                                });
                                let genreArray = genreResponse.data.map(
                                    (genre) => genre.id
                                );
                                $("#series_set_selectGenre")
                                    .val(genreArray)
                                    .selectric("refresh");
                                let originalLanguageCode =
                                    response.original_language;
                                $.ajax({
                                    type: "POST",
                                    url: `${domainUrl}addLanguage`,
                                    data: {
                                        languageCode: originalLanguageCode,
                                    },
                                    success: function (languageResponse) {
                                        if (languageResponse.status) {
                                            $(
                                                "#series_set_language_id"
                                            ).empty();
                                            languageResponse.allLanguages.forEach(
                                                function (language) {
                                                    $(
                                                        "#series_set_language_id"
                                                    ).append(
                                                        new Option(
                                                            language.title,
                                                            language.id
                                                        )
                                                    );
                                                }
                                            );
                                            let languageId =
                                                languageResponse.data;
                                            $("#series_set_language_id")
                                                .val(languageId)
                                                .selectric("refresh");
                                            $(".saveButton").removeClass(
                                                "spinning disabled"
                                            );
                                            $(
                                                ".importSeriesContentData"
                                            ).removeClass("spinning disabled");
                                            $("#setSeriesContentModal").modal(
                                                "show"
                                            );
                                        } else {
                                            console.log(
                                                "Error adding language"
                                            );
                                        }
                                    },
                                    error: function (error) {
                                        console.error(
                                            "Error adding language:",
                                            error
                                        );
                                    },
                                });
                            } else {
                                console.log("Error adding genres");
                            }
                        },
                        error: function (error) {
                            console.error("Error adding genres:", error);
                        },
                    });
                } else {
                    console.log("No genres found for this movie.");
                    $(".saveButton").removeClass("spinning disabled");
                    $(".importSeriesContentData").removeClass(
                        "spinning disabled"
                    );
                    $("#setSeriesContentModal").modal("show");
                }
            },
            error: function (error) {
                console.error("Error fetching series details:", error);
            },
        });
    });

    $(document).on("submit", "#addNewContentForm", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addNewContentForm")[0]);

            let verticalPosterSrc = $("#set_vertical_poster").attr("src");
            let horizontalPosterSrc = $("#set_horizontal_poster").attr("src");
            let placeholderSrc = "assets/img/placeholder-image.png";

            if (verticalPosterSrc === placeholderSrc) {
                alert("Please select vertical poster images.");
                $(".saveButton").removeClass("spinning disabled");
                return;
            }
            if (horizontalPosterSrc === placeholderSrc) {
                alert("Please select horizontal poster images.");
                $(".saveButton").removeClass("spinning disabled");
                return;
            }

            if ($("#set_vertical_poster").attr("data-url") === "true") {
                formData.append("vertical_poster_url", verticalPosterSrc);
            }
            if ($("#set_horizontal_poster").attr("data-url") === "true") {
                formData.append("horizontal_poster_url", horizontalPosterSrc);
            }

            $.ajax({
                type: "POST",
                url: `${domainUrl}addNewContent`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        window.location.href = `${domainUrl}contentList`;
                    }
                },
            });
        });
    });

    $(document).on("submit", "#addNewContentFormSeries", function (e) {
        e.preventDefault();
        checkUserType(function (e) {
            let formData = new FormData($("#addNewContentFormSeries")[0]);

            let verticalPosterSrc = $("#series_set_vertical_poster").attr(
                "src"
            );
            let horizontalPosterSrc = $("#series_set_horizontal_poster").attr(
                "src"
            );
            let placeholderSrc = "assets/img/placeholder-image.png";

            if (verticalPosterSrc === placeholderSrc) {
                alert("Please select vertical poster images.");
                $(".saveButton").removeClass("spinning disabled");
                return;
            }
            if (horizontalPosterSrc === placeholderSrc) {
                alert("Please select horizontal poster images.");
                $(".saveButton").removeClass("spinning disabled");
                return;
            }

            if ($("#series_set_vertical_poster").attr("data-url") === "true") {
                formData.append("vertical_poster_url", verticalPosterSrc);
            }
            if (
                $("#series_set_horizontal_poster").attr("data-url") === "true"
            ) {
                formData.append("horizontal_poster_url", horizontalPosterSrc);
            }

            $.ajax({
                type: "POST",
                url: `${domainUrl}addNewContent`,
                data: formData,
                contentType: false,
                processData: false,
                success: function (response) {
                    if (response.status) {
                        window.location.href = `${domainUrl}contentList?tab=series`;
                    }
                },
            });
        });
    });
});
