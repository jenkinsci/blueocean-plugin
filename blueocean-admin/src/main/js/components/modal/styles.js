
const styles = {
  overlayStyles: {
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100%',
    height: '100%',
    zIndex: 99,
    backgroundColor: 'rgba(0,0,0,0.3)'
  },
  dialogStyles: {
    width: '98%',
    height: '98%',
    position: 'absolute',
    top: '1%',
    left: '1%',
    backgroundColor: '#fff',
    borderRadius: '2px',
    zIndex: 100,
    boxShadow: '0 0 4px rgba(0,0,0,.14),0 4px 8px rgba(0,0,0,.28)'
  },
  title: {
    marginTop: '0'
  },
  closeButtonStyle: {
    cursor: 'pointer',
    position: 'absolute',
    fontSize: '3em',
    color: '#ffffff',
    right: '20px',
    top: '5px'
  }
};

export default styles;
