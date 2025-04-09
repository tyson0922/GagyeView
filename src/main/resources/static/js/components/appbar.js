//===== close navbar-collapse when a  clicked
document.addEventListener("DOMContentLoaded", function () {
    let navbarTogglerOne = document.querySelector(".navbar-one .navbar-toggler");
    if (navbarTogglerOne) {
        navbarTogglerOne.addEventListener("click", function () {
            navbarTogglerOne.classList.toggle("active");
        });
    }
});