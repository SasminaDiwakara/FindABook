class footerContent extends HTMLElement{
    connectedCallback(){
        this.innerHTML=' <div class="container">\n' +
            '            <div class="row">\n' +
            '                <div class="col-md-4 mb-4">\n' +
            // '                    <a href="/" class="navbar-brand">\n' +
            // '                        <img src="assets/images/findabookLogo.png" alt="findabook Logo" class="logo-img">\n' +
            // '                    </a>\n' +
            '                    <p>Find your next favorite read and explore thousands of books across all genres. Join our community\n' +
            '                        of passionate readers today.</p>\n' +
            '                    <div class="newsletter-form">\n' +
            '                        <input type="email" class="newsletter-input" placeholder="Enter your email">\n' +
            '                        <button class="newsletter-btn">Subscribe</button>\n' +
            '                    </div>\n' +
            '                </div>\n' +
            '                <div class="col-md-2 mb-4">\n' +
            '                    <h5>Quick Links</h5>\n' +
            '                    <ul class="list-unstyled">\n' +
            '                        <li class="mb-2"><a href="/">Home</a></li>\n' +
            '                        <li class="mb-2"><a href="/catalog">Catalog</a></li>\n' +
            '                        <li class="mb-2"><a href="/categories">Categories</a></li>\n' +
            '                        <li class="mb-2"><a href="#contact">Contact</a></li>\n' +
            '                    </ul>\n' +
            '                </div>\n' +
            '                <div class="col-md-3 mb-4">\n' +
            '                    <h5>Categories</h5>\n' +
            '                    <ul class="list-unstyled">\n' +
            '                        <li class="mb-2"><a href="/fiction">Fiction</a></li>\n' +
            '                        <li class="mb-2"><a href="/self-help">Self-Help</a></li>\n' +
            '                        <li class="mb-2"><a href="/business">Business</a></li>\n' +
            '                        <li class="mb-2"><a href="/technology">Technology</a></li>\n' +
            '                    </ul>\n' +
            '                </div>\n' +
            '                <div class="col-md-3 mb-4">\n' +
            '                    <h5>Follow Us</h5>\n' +
            '                    <p style="margin-bottom: 12px;">Join our social community for updates and book recommendations.</p>\n' +
            '                    <div class="social-links">\n' +
            '                        <a href="#"><i data-feather="facebook"></i></a>\n' +
            '                        <a href="#"><i data-feather="instagram"></i></a>\n' +
            '                        <a href="#"><i data-feather="twitter"></i></a>\n' +
            '                        <a href="#"><i data-feather="youtube"></i></a>\n' +
            '                    </div>\n' +
            '                </div>\n' +
            '            </div>\n' +
            '            <hr>\n' +
            '            <div class="footer-bottom">\n' +
            '                <p>&copy; 2026 FindABook. All rights reserved.</p>\n' +
            '                <div class="footer-links">\n' +
            '                    <a href="/privacy">Privacy Policy</a>\n' +
            '                    <a href="/terms">Terms of Service</a>\n' +
            '                    <a href="/cookies">Cookie Policy</a>\n' +
            '                </div>\n' +
            '            </div>\n' +
            '        </div>';
    }
}

customElements.define('footer-content', footerContent);