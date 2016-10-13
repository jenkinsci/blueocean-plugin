const PropertiesReader = require('properties-reader');

const properties = PropertiesReader('../resources/common.properties');
console.log('mmmmmmmm', properties.getAllProperties());