export default function fetchData(onLoad, url, toJson = true) {
    const xmlhttp = new XMLHttpRequest();
    const requestDone = 4; // Because Zombie is garbage

    if (!url) {
        onLoad(null);
        return;
    }

    xmlhttp.onreadystatechange = () => {
        if (xmlhttp.readyState === requestDone) {
            if (xmlhttp.status === 200) {
                let data = null;
                try {
                    if (toJson) {
                        data = JSON.parse(xmlhttp.responseText);
                    } else {
                        data = xmlhttp.responseText;
                    }
                } catch (e) {
                    // eslint-disable-next-line
                    console.log('Loading', url,
                        'Expecting JSON, instead got', xmlhttp.responseText, e);
                }
                onLoad(data);
            } else {
                // eslint-disable-next-line
                console.log('something else other than 200 was returned');
            }
        }
    };
    xmlhttp.open('GET', url, true);
    xmlhttp.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');
    xmlhttp.send();
}
