var gulp = require('gulp');
var concat = require('gulp-concat');
var mainBowerFiles = require('main-bower-files');
var filter = require('gulp-filter');

var print = require('gulp-print')

gulp.task('hello', function() {
	console.log("Hello World!");
});

gulp.task('default', ['css', 'scripts']);

gulp.task('scripts', function(){
	return gulp.src(mainBowerFiles())
		.pipe(filter('*.js'))
		.pipe(gulp.dest('./dist/js'));
});

gulp.task('css', function(){
	return gulp.src(mainBowerFiles())
		.pipe(filter('*.css'))
		.pipe(gulp.dest('./dist/css'));
});
