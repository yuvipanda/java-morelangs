var fs = require('fs');
var xmlbuilder = require('xmlbuilder');
var _ = require('underscore');
var findit = require('findit');
var path = require('path');

var inputPath = process.argv[2];
var outputPath = process.argv[3];

var methodsIndex = [];

function attrsFromRules( rules ) {
    var attrs = {
        id: rules.id,
        name: rules.name,
        description: rules.description,
        author: rules.author,
        version: rules.version,
        contextLength: rules.contextLength,
        maxKeyLength: rules.maxKeyLength
    };

    _.each( attrs, function( value, key ) {
        if( typeof value === 'undefined' || value === null) {
            delete attrs[ key ];
        }
    } );
    return attrs;
}

function processRules( rules ) {
    jQuery.ime.inputmethods[ rules.id ] = rules;

    var attrs = attrsFromRules( rules );
    var xml = xmlbuilder.create('inputmethod');
    _.each( rules.patterns, function( pattern ) {
        if( pattern.length === 2) {
            xml.ele( 'pattern', {
                input: pattern[0],
                replacement: pattern[1]
            } );
        } else {
            xml.ele( 'pattern', {
                input: pattern[0],
                context: pattern[1],
                replacement: pattern[2]
            } );
        }
    } );
    xml.root();
    _.each( attrs, function( value, key ) {
        xml.att( key, value );
    } );

    attrs.filename = rules.id + '.xml';
    methodsIndex.push( attrs );
    console.log( methodsIndex );
    var outputFile = path.join( outputPath, attrs.filename );
    var xmlStr = xml.end( { pretty: true } );

    fs.writeFile( outputFile, xmlStr, function( err ) {
        if( err ) {
            console.log( err );
        }
    } );
}

// Stub jQuery object, has the only method that we want
// Then we eval the data of the rules file
var jQuery = {
    ime: {
        register: processRules,
        inputmethods: {},
        languages: {},
        sources: {}
    },
    each: function( iter, fun ) {
        _.each( iter, function( value, key ) { 
            fun( key, value );
        } );
    },
    extend: _.extend
};

findit.find( inputPath ).on( 'file', function( fileName, stat ) {
    if( fileName.match( /\.js$/ ) ) {
        fs.readFile( fileName, 'utf8', function( err, data ) {
            console.log( "Trying to read " + fileName );
            eval( data ); 
        } );
    }
} ).on( 'end', function() {

    fs.readFile( path.join( inputPath, '../src/jquery.ime.inputmethods.js' ), 'utf8', function( err, data ) {
        eval( data );

        var languagesXML = xmlbuilder.create('languages');

        _.each( jQuery.ime.languages, function( value, key ) {
            var langXML = languagesXML.ele( 'language' ).att( 'code', key ).att( 'autonym', value.autonym );
            _.each( value.inputmethods, function( imName ) {
                var imXML = langXML.ele( 'inputMethod' );
                var attrs = attrsFromRules( jQuery.ime.inputmethods[ imName ] );
                _.each( attrs, function( value, key ) {
                    imXML.att( key, value );
                } );
            } );
        } );
        

        var xmlString = languagesXML.end( { pretty: true } );
        fs.writeFile( path.join( outputPath, 'languages.xml' ), xmlString, function( err ) {
            if( err ) {
                console.log( err );
            }
        } );
    } );
} );
