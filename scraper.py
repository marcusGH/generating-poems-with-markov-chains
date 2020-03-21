from lxml import html
import requests

url1 = "https://dikt.org/Henrik_Wergeland_-_Samlede_Skrifter_-_1._bind_1825-1833"
url2 = "https://dikt.org/Henrik_Wergeland_-_Samlede_Skrifter_-_2._bind_1833-1841"

urls = [url1, url2]
base_url = "https://dikt.org"

# fetch the pages
pages = [requests.get(url) for url in urls]
trees = [html.fromstring(page.content) for page in pages]

# find the poems
relative_poem_urlss = [tree.xpath("//table//a/@href") for tree in trees]

# don't want these
ignore = ['\n', '\t', '\xa0']
def scrape_poem(url):
    page = requests.get(url)
    tree = html.fromstring(page.content)

    lines = tree.xpath("//div[@id='mw-content-text']//p/text()")
    # remove stuff to ignore
    for i in range(len(lines)):
        for ig in ignore:
            lines[i] = lines[i].replace(ig, "")
    # remove empty lines
    lines = list(filter(lambda a: a != "", lines))

    return lines

i = 0
numPoems = sum([len(rel_poems) for rel_poems in relative_poem_urlss])
for relative_poems in relative_poem_urlss:
    for j in range(len(relative_poems)):
        print(f"{i}/{numPoems} poems scraped")
        # make a file in a data directory
        with open(f"data/{i}", "w") as f:
            poem = scrape_poem(base_url +  relative_poems[j])
            for line in poem:
                f.write(line + " EOL ")
        i += 1
