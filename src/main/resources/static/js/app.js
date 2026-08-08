(function () {
    "use strict";

    function closeOffcanvasOnNavigate() {
        document.querySelectorAll(".panel-offcanvas .list-group-item-action[href]").forEach(function (link) {
            link.addEventListener("click", function () {
                var offcanvas = link.closest(".offcanvas");
                if (offcanvas && window.bootstrap && window.bootstrap.Offcanvas) {
                    var instance = window.bootstrap.Offcanvas.getInstance(offcanvas);
                    if (instance) {
                        instance.hide();
                    }
                }
            });
        });
    }

    function enhanceTables() {
        document.querySelectorAll(".table-responsive").forEach(function (wrapper) {
            wrapper.setAttribute("tabindex", "0");
            if (!wrapper.getAttribute("aria-label")) {
                wrapper.setAttribute("aria-label", "Tabela com deslocação horizontal");
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        closeOffcanvasOnNavigate();
        enhanceTables();
    });
})();
