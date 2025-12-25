// Thymeleaf Preview Sample JavaScript

document.addEventListener('DOMContentLoaded', function() {
    console.log('Thymeleaf Preview Tool - Sample Page Loaded');

    // Add smooth scroll behavior
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });

    // Add active class to current nav link
    const currentPath = window.location.search;
    document.querySelectorAll('.nav-links a').forEach(link => {
        if (link.href.includes(currentPath) && currentPath) {
            link.style.fontWeight = '700';
        }
    });

    // Form validation
    const form = document.querySelector('.contact-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            alert('This is a preview - form submission is disabled.');
        });
    }
});
