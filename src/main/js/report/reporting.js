export function getMetaContentByName(name, content) {
    const safeContent = content ? content : "content";
    const doc = document.querySelector("meta[name='" + name + "']")
    return doc ? doc.getAttribute(safeContent) : '';
}

// Works well accross all major browsers.
export function downloadReport(reportName, contentType, byte) {
    let blob = new Blob([byte], {type: contentType});
    let link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = reportName;
    link.click();
}

export default function generateReport(sprintInformation, token) {

    if (!sprintInformation)
        throw Error('Report information was not set');
    if (!token)
        throw Error('Auth token was not set');

    const jsonData = JSON.stringify(sprintInformation);
    const finalUrl = `generate?jwt=${token}&data=${encodeURIComponent(jsonData)}`;

    return fetch(finalUrl)
        .then(response => {
            if (response.status === 200) {
                return response.arrayBuffer();
            } else {
                return response.json();
            }
        });
};
