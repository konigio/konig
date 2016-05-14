var gulp = require('gulp');
var concat = require('gulp-concat');
var mainBowerFiles = require('main-bower-files');
var filter = require('gulp-filter');
var minify = require('gulp-minify');

var print = require('gulp-print')

gulp.task('hello', function() {
	console.log("Hello World!");
});

gulp.task('default', ['css', 'minify', 'copy-files']);

gulp.task('minify', function(){
	return gulp.src([
           './src/js/jquery.js',
           './src/js/jquery-ui.js', 
           './src/js/jquery.layout.min.js',
           './src/js/mustache.js',
           './src/js/sha1.js',
           './src/js/springy.js',
           './src/js/springyui.js',
           './src/js/uuid.js',
           './src/js/konig-jsonld.js',
           './src/js/konig-rdf-model.js',
           './src/js/konig-context.js',
           './src/js/konig-history-manager.js',
           './src/js/konig-rdfspringy.js',
           './src/js/konig-ontodoc.js',
           './src/js/konig-ajax-ontology-service.js'
       ])
		.pipe(concat('ontodoc-all.js',{'newline': '\n'}))
		.pipe(minify({ignoreFiles: ['-min.js']}))
		.pipe(gulp.dest('./dist/js/'));
});

gulp.task('copy-files' , function() {
	gulp.src('./src/ontodoc.html')
		.pipe(gulp.dest('./dist/'));
});

gulp.task('css', function(){
	return gulp.src(['./src/css/**'], {base: './src/css/'})
		.pipe(gulp.dest('./dist/css'));
});

