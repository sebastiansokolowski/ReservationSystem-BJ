(function () {
    "use strict";

    function ready(callback) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", callback);
        } else {
            callback();
        }
    }

    ready(function () {
        var cookieBanner = document.querySelector("[data-cookie-banner]");
        var cookieAccept = document.querySelector("[data-cookie-accept]");

        if (cookieBanner && cookieAccept) {
            var consentKey = "bj-cookie-consent";
            var hasConsent = false;

            try {
                hasConsent = window.localStorage.getItem(consentKey) === "accepted";
            } catch (error) {
                hasConsent = document.cookie.indexOf("bj_cookie_consent=accepted") !== -1;
            }

            if (!hasConsent) {
                cookieBanner.hidden = false;
            }

            cookieAccept.addEventListener("click", function () {
                try {
                    window.localStorage.setItem(consentKey, "accepted");
                } catch (error) {
                    document.cookie = "bj_cookie_consent=accepted; Max-Age=31536000; Path=/; SameSite=Lax";
                }
                cookieBanner.hidden = true;
            });
        }

        var conditionalToggles = document.querySelectorAll("[data-enable-target]");
        Array.prototype.forEach.call(conditionalToggles, function (toggle) {
            var selectors = toggle.getAttribute("data-enable-target").split(",");
            var update = function () {
                selectors.forEach(function (selector) {
                    var field = document.querySelector(selector.trim());
                    if (field) {
                        field.disabled = !toggle.checked;
                    }
                });
            };
            toggle.addEventListener("change", update);
            update();
        });

        var mobileMenu = document.querySelector("[data-mobile-menu]");
        var mobileMenuToggle = document.querySelector("[data-mobile-menu-toggle]");

        if (mobileMenu && mobileMenuToggle) {
            var updateMobileMenuState = function () {
                var isOpen = mobileMenu.hasAttribute("open");
                mobileMenuToggle.setAttribute("aria-expanded", isOpen ? "true" : "false");
                mobileMenuToggle.classList.toggle("is-active", isOpen);
            };

            mobileMenuToggle.addEventListener("click", function () {
                mobileMenu.open = !mobileMenu.open;
                updateMobileMenuState();
            });

            mobileMenu.addEventListener("toggle", updateMobileMenuState);

            document.addEventListener("click", function (event) {
                if (mobileMenu.open && !mobileMenu.contains(event.target) && !mobileMenuToggle.contains(event.target)) {
                    mobileMenu.open = false;
                }
            });

            document.addEventListener("keydown", function (event) {
                if (event.key === "Escape" && mobileMenu.open) {
                    mobileMenu.open = false;
                    mobileMenuToggle.focus();
                }
            });

            updateMobileMenuState();
        }

        var forms = document.querySelectorAll("form[data-confirm]");
        if (!forms.length) {
            return;
        }

        var overlay = document.createElement("div");
        overlay.className = "confirm-overlay";
        overlay.setAttribute("role", "dialog");
        overlay.setAttribute("aria-modal", "true");
        overlay.innerHTML =
            '<div class="confirm-box">' +
                '<div class="confirm-box__icon">✓</div>' +
                '<h2 data-confirm-title></h2>' +
                '<p class="muted" data-confirm-message></p>' +
                '<div class="confirm-box__actions">' +
                    '<button type="button" class="btn btn--secondary" data-confirm-cancel></button>' +
                    '<button type="button" class="btn" data-confirm-submit></button>' +
                '</div>' +
            '</div>';
        document.body.appendChild(overlay);

        var confirmTitle = document.querySelector('meta[name="i18n-confirm-title"]');
        var confirmCancel = document.querySelector('meta[name="i18n-confirm-cancel"]');
        var confirmSubmit = document.querySelector('meta[name="i18n-confirm-submit"]');
        overlay.querySelector("[data-confirm-title]").textContent = confirmTitle ? confirmTitle.content : "Confirm action";
        overlay.querySelector("[data-confirm-cancel]").textContent = confirmCancel ? confirmCancel.content : "Cancel";
        overlay.querySelector("[data-confirm-submit]").textContent = confirmSubmit ? confirmSubmit.content : "Confirm";

        var activeForm = null;
        var message = overlay.querySelector("[data-confirm-message]");

        Array.prototype.forEach.call(forms, function (form) {
            form.addEventListener("submit", function (event) {
                if (form.getAttribute("data-confirmed") === "true") {
                    return;
                }
                event.preventDefault();
                activeForm = form;
                message.textContent = form.getAttribute("data-confirm");
                overlay.classList.add("is-open");
            });
        });

        overlay.querySelector("[data-confirm-cancel]").addEventListener("click", function () {
            overlay.classList.remove("is-open");
            activeForm = null;
        });

        overlay.querySelector("[data-confirm-submit]").addEventListener("click", function () {
            if (activeForm) {
                activeForm.setAttribute("data-confirmed", "true");
                activeForm.submit();
            }
        });

        overlay.addEventListener("click", function (event) {
            if (event.target === overlay) {
                overlay.classList.remove("is-open");
                activeForm = null;
            }
        });
    });
}());
