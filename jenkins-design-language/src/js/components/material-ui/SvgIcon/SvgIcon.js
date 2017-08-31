import React, {Component, PropTypes } from 'react';

class SvgIcon extends Component {

  static propTypes = {
    /**
     * Elements passed into the SVG Icon.
     */
    children: PropTypes.node,
    /**
     * This is the fill color of the svg icon.
     * If not specified, this component will default
     * to white.
     */
    color: PropTypes.string,
    /** @ignore */
    onMouseEnter: PropTypes.func,
    /** @ignore */
    onMouseLeave: PropTypes.func,
    /**
     * Override the inline-styles of the root element.
     */
    size: PropTypes.number,
    style: PropTypes.object,
    /**
     * Allows you to redefine what the coordinates
     * without units mean inside an svg element. For example,
     * if the SVG element is 500 (width) by 200 (height), and you
     * pass viewBox="0 0 50 20", this means that the coordinates inside
     * the svg will go from the top left corner (0,0) to bottom right (50,20)
     * and each unit will be worth 10px.
     */
    viewBox: PropTypes.string,
  };

  static defaultProps = {
    onMouseEnter: () => {},
    onMouseLeave: () => {},
    viewBox: '0 0 24 24',
  }; 

  state = {
    hovered: false,
  };

  handleMouseLeave = (event) => {
    this.props.onMouseLeave(event);
  };

  handleMouseEnter = (event) => {
    this.props.onMouseEnter(event);
  };

  render() {
    const {
      children,
      color,
      hoverColor,
      onMouseEnter, // eslint-disable-line no-unused-vars
      onMouseLeave, // eslint-disable-line no-unused-vars
      size,
      style,
      viewBox,
      ...other
    } = this.props;

    const mergedStyles = Object.assign({
      display: 'inline-block',
      height: size ? size : 24,
      width: size ? size : 24,
      verticalAlign: 'middle',
      userSelect: 'none',
    }, style);

    delete other.iconName;

    return (
      <svg
        {...other}
        onMouseEnter={this.handleMouseEnter}
        onMouseLeave={this.handleMouseLeave}
        fill={color ? color : 'white'}
        style={mergedStyles}
        viewBox={viewBox}
      >
        {children}
      </svg>
    );
  }
}

export default SvgIcon;
