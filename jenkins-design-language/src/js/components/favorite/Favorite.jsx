// @flow

import React, { Component, PropTypes } from 'react';
import { Checkbox } from '../forms/Checkbox';

export class Favorite extends Component {

    static defaultProps = {
        checked: false,
        className: ''
    };

    checkbox:Checkbox;

    get checked():boolean {
        return this.checkbox.checked;
    }

    render() {
        const extraClass = this.props.className || '';

        return (
            <Checkbox
                ref={checkbox => { this.checkbox = checkbox; }}
                className={`Favorite ${extraClass}`}
                checked={this.props.checked}
                onToggle={this.props.onToggle}
                label={this.props.label}
            >
                <FavoriteStarSvg checked={this.props.checked} />
            </Checkbox>
        );
    }

}

Favorite.propTypes = {
    className: PropTypes.oneOf(['', 'dark']),
    checked: PropTypes.bool,
    label: PropTypes.string,
    onToggle: PropTypes.func
};

/* eslint-disable max-len */
function FavoriteStarSvg(checkedObj) {
    let checked = checkedObj.checked;

    return (
        <div title={checked ? 'Remove Favourite' : 'Favourite'}>
            <svg className="star-icon" width="288" height="24" viewBox="0 0 288 24" xmlns="http://www.w3.org/2000/svg"
                focusable="false"
            >
                <title>stars for light background</title>
                <g fill="none" fillRule="evenodd">
                    { /* this star is used for the default 'unchecked' state */ }
                    <path className="Favorite-fill star-empty" d="M22 9.24l-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24zM12 15.4l-3.76 2.27 1-4.28-3.32-2.88 4.38-.38L12 6.1l1.71 4.04 4.38.38-3.32 2.88 1 4.28L12 15.4z"/>
                    <path d="M0 0h24v24H0V0zM49-1h24v24H49V-1z"/>
                    <path d="M49-1h24v24H49V-1zM144 0h24v24h-24V0z"/>
                    <path d="M144 0h24v24h-24V0z"/>
                    <g className="Favorite-stroke--selected" strokeWidth=".8" strokeLinecap="square">
                        <path d="M153 8l-1.5-1.5M159 8l1.5-1.5M151.051 14.054l-1.576 1.42M162.46 15.038L161 14M155.691 18.849l.02-1.12"/>
                    </g>
                    <path d="M156 15.689l4.326 2.611-1.148-4.921L163 10.068l-5.033-.427L156 5l-1.967 4.641-5.033.427 3.822 3.311-1.148 4.921L156 15.689z" className="Favorite-fill--selected"/>
                    <path d="M24 0h24v24H24V0z"/>
                    <path d="M24 0h24v24H24V0z"/>
                    <path d="M36 14.635l3.09 1.865-.82-3.515L41 10.62l-3.595-.305L36 7l-1.405 3.315L31 10.62l2.73 2.365-.82 3.515L36 14.635z" className="Favorite-fill--selected" opacity=".72"/>
                    <path d="M48 0h24v24H48V0z"/>
                    <path d="M48 0h24v24H48V0z"/>
                    <path d="M60.5 15.399l3.399 2.051-.902-3.866L66 10.982l-3.954-.335L60.5 7l-1.546 3.646-3.954.336 3.003 2.602-.902 3.866 3.399-2.051z" className="Favorite-fill--selected" opacity=".82"/>
                    <path d="M72 0h24v24H72V0z"/>
                    <path d="M72 0h24v24H72V0z"/>
                    <path d="M84 15.162l3.708 2.238-.984-4.218L90 10.344l-4.314-.366L84 6l-1.686 3.978-4.314.366 3.276 2.838-.984 4.218L84 15.162z" className="Favorite-fill--selected" opacity=".85"/>
                    <path d="M96 0h24v24H96V0z"/>
                    <path d="M96 0h24v24H96V0z"/>
                    <path d="M108.5 15.925l4.017 2.425-1.066-4.57L115 10.707l-4.674-.396L108.5 6l-1.826 4.31-4.674.396 3.549 3.075-1.066 4.569 4.017-2.425z" className="Favorite-fill--selected"/>
                    <g className="Favorite-stroke--selected" strokeWidth=".5" strokeLinecap="square">
                        <path d="M107 11l-1.5-1.5M110 11l1.5-1.5M106.051 13.054l-1.576 1.42M112.46 14.038L111 13M108.691 16.849l.02-1.12"/>
                    </g>
                    <g>
                        <path d="M120 0h24v24h-24V0z"/>
                        <path d="M120 0h24v24h-24V0z"/>
                        <path d="M131.75 15.307l4.171 2.518-1.106-4.745 3.685-3.193-4.853-.412L131.75 5l-1.897 4.475-4.853.412 3.685 3.193-1.107 4.745 4.172-2.518z" className="Favorite-fill--selected"/>
                        <g className="Favorite-stroke--selected" strokeWidth=".5" strokeLinecap="square">
                            <path d="M129 8l-1.5-1.5M135 8l1.5-1.5M127.051 14.054l-1.412 1.271M138.46 15.038L137 14M131.691 18.849l.02-1.12"/>
                        </g>
                    </g>
                    <g>
                        <path d="M168 0h24v24h-24V0z"/>
                        <path d="M168 0h24v24h-24V0z"/>
                        <g className="Favorite-stroke--selected" strokeWidth=".8" strokeLinecap="square">
                            <path d="M177 8l-1.5-1.5M183 8l1.5-1.5M175.051 14.054l-1.576 1.42M186.46 15.038L185 14M180.396 18.849l.008-1.12"/>
                        </g>
                        <path d="M180 16.743l5.562 3.357-1.476-6.327L189 9.516l-6.471-.549L180 3l-2.529 5.967-6.471.549 4.914 4.257-1.476 6.327L180 16.743z" className="Favorite-fill--selected"/>
                    </g>
                    <g>
                        <path d="M192 0h24v24h-24V0z"/>
                        <path d="M204 17.27l6.18 3.73-1.64-7.03L214 9.24l-7.19-.61L204 2l-2.81 6.63-7.19.61 5.46 4.73-1.64 7.03 6.18-3.73z" className="Favorite-fill--selected"/>
                        <path d="M192 0h24v24h-24V0z"/>
                        <g className="Favorite-stroke--selected" strokeLinecap="square">
                            <path d="M201 8l-1.5-1.5M207 8l1.5-1.5M199.051 14.054l-1.576 1.42M210.46 15.038L209 14M203.495 18.849l.01-1.12"/>
                        </g>
                    </g>
                    <g>
                        <path d="M216 0h24v24h-24V0z"/>
                        <path d="M228 18.324l7.416 4.476-1.968-8.436L240 8.688l-8.628-.732L228 0l-3.372 7.956-8.628.732 6.552 5.676-1.968 8.436L228 18.324z" className="Favorite-fill--selected"/>
                        <path d="M216 0h24v24h-24V0z"/>
                    </g>
                    <g>
                        <path d="M264 0h24v24h-24V0z"/>
                        { /* this star is used for the component's 'checked state' */ }
                        <path className="star-filled Favorite-fill--selected" d="M276 17.27l6.18 3.73-1.64-7.03L286 9.24l-7.19-.61L276 2l-2.81 6.63-7.19.61 5.46 4.73-1.64 7.03 6.18-3.73z"/>
                        <path d="M264 0h24v24h-24V0z"/>
                    </g>
                    <g>
                        <path d="M240 0h24v24h-24V0z"/>
                        <path d="M252 17.797l6.798 4.103-1.804-7.733L263 8.964l-7.909-.671L252 1l-3.091 7.293-7.909.671 6.006 5.203-1.804 7.733L252 17.797z" className="Favorite-fill--selected"/>
                        <path d="M240 0h24v24h-24V0z"/>
                    </g>
                    <circle className="Favorite-stroke--selected" strokeWidth=".5" cx="156" cy="12" r="8"/>
                    <circle className="Favorite-stroke--selected" strokeWidth=".3" cx="180" cy="12" r="10"/>
                    <circle className="Favorite-stroke--selected" strokeWidth=".1" cx="204" cy="12" r="11"/>
                    <circle className="Favorite-stroke--selected" strokeWidth=".8" cx="132" cy="12" r="7"/>
                    <circle className="Favorite-stroke--selected" cx="108.5" cy="12.5" r="5.5"/>
                </g>
            </svg>
        </div>
    );
}
/* eslint-enable max-len */
