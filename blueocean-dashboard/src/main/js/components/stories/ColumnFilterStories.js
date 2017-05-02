/* eslint-disable */
import React, {Component} from 'react';
import {storiesOf, action} from '@kadira/storybook';
import {ColumnFilter} from '../ColumnFilter'

storiesOf('ColumnFilter', module)
    .add('Toggle', basic)
;

function wrap(content) {
    const wrapperStyle = {
        margin: '2em'
    };
    return (
        <div style={wrapperStyle}>{content}</div>
    );
}

function columnFilterChange(...args) {
    console.log('columnFilterChange', args);
}

const people = ["Homer Simpson", "Marge Simpson", "Bart Simpson", "Lisa Simpson", "Maggie Simpson", "Akira",
    "Jasper Beardly", "Wendell Borton", "Patty Bouvier", "Selma Bouvier", "Kent Brockman", "Bumblebee Man",
    "Charles Montgomery Burns", "Capital City Goofball", "Carl Carlson", "Crazy Cat Lady",
    "Superintendent Gary Chalmers", "Comic Book Guy", "Disco Stu", "Dolph", "Lunchlady Doris", "Duffman", "Eddie",
    "Lou", "Fat Tony", "Ned Flanders", "Rod Flanders", "Todd Flanders", "Frankie the Squealer", "Professor John Frink",
    "Barney Gumble", "Gil Gunderson", "Dr. Julius Hibbert", "Lionel Hutz", "Jacques", "Jimbo Jones", "Kang", "Kodos",
    "Kearney Zzyzwicz", "Kearney Zzyzwicz Jr.", "Edna Krabappel", "Rabbi Hyman Krustofski", "Krusty the Clown",
    "Cookie Kwan", "Lenny Leonard", "Lewis", "Helen Lovejoy", "Reverend Timothy Lovejoy", "Coach Lugash", "Luigi",
    "Lurleen Lumpkin", "Otto Mann", "Troy McClure", "Hans Moleman", "Dr. Marvin Monroe", "Nelson Muntz",
    "Bleeding Gums Murphy", "Lindsey Naegle", "Apu Nahasapeemapetilon", "Arnie Pye", "Herbert Powell", "Martin Prince",
    "Mayor \"Diamond Joe\" Quimby", "Radioactive Man", "The Rich Texan", "Dr. Nick Riviera", "Santa's Little Helper",
    "Sherri and Terri", "Sideshow Bob", "Sideshow Mel", "Grampa Abraham Simpson", "Agnes Skinner",
    "Principal Seymour Skinner", "Waylon Smithers", "Snake Jailbird", "Judge Roy Snyder", "Jebediah Springfield",
    "Cletus Spuckler", "Brandine Spuckler", "Squeaky-Voiced Teen", "Moe Szyslak", "Mr. Teeny", "Cecil Terwilliger",
    "Johnny Tightlips", "Üter", "Kirk Van Houten", "Luann Van Houten", "Milhouse Van Houten", "Chief Clancy Wiggum",
    "Ralph Wiggum", "Groundskeeper Willie", "Wiseguy", "Rainier Wolfcastle", "Artie Ziff"];

function basic() {

    const nestedDivStyle = {
        border: "solid 1px #ccc",
        padding: "1em",
        width: "20em",
        overflow: "hidden"
    };

    const spacingStyle = {
        padding: "50em 5em"
    };

    return wrap(
        <section>
            <h1>Basic</h1>
            <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Ab alias aliquam asperiores consequuntur eius esse fuga illum incidunt iure labore maiores maxime, neque officia omnis provident sequi sunt temporibus ullam!</p>
            <div style={nestedDivStyle}>
                <ColumnFilter placeholder="Placeholder" options={people} onChange={columnFilterChange}/>
            </div>
            <p style={spacingStyle}>Spacing for scrolling</p>
        </section>
    );
}
