package hello

import (
	"appengine"
	"appengine/datastore"
	//"appengine/user"
	"bytes"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strconv"
	"strings"
)

type Griddler struct {
	Id         string
	Author     string
	Name       string
	Rank       string
	Difficulty string
	Width      string
	Height     string
	Solution   string
}

type GriddlerTag struct {
	Tag        string
	GriddlerId string
}

type GriddlerUser struct {
	Username string
}

func createHandler(w http.ResponseWriter, r *http.Request) {
	//Creating a Griddler to add to datastore.
	c := appengine.NewContext(r)
	parsed, err := url.Parse(r.URL.String())

	if err != nil {
		fmt.Fprintln(w, "Bad stuff happened in the create handler\t")
		return
	}
	q := parsed.Query() //Parse the URL.
	tags := new(GriddlerTag)
	griddler := new(Griddler) //Create and set all the data in your griddler.
	griddler.Id = q.Get("id")
	tags.GriddlerId = q.Get("id")
	griddler.Author = q.Get("author")
	griddler.Name = q.Get("name")
	griddler.Rank = q.Get("rank")
	griddler.Difficulty = q.Get("diff")
	griddler.Width = q.Get("width")
	griddler.Height = q.Get("height")
	griddler.Solution = q.Get("solution")
	rawTags := strings.Split(q.Get("tags"), ",")
	for _, value := range rawTags {
		//For each rawTag, add a GriddlerTag object.
		tags.Tag = value
		key := datastore.NewKey(c, "GriddlerTag", tags.GriddlerId+tags.Tag, 0, nil)
		if _, err := datastore.Put(c, key, tags); err != nil {
			fmt.Fprintf(w, "Error adding a tag: %s", value)
		}
	}
	k := datastore.NewKey(c, "Griddler", griddler.Id, 0, nil) //Make a key based on the Id of the Griddler.
	if _, err := datastore.Put(c, k, griddler); err != nil {
		fmt.Fprint(w, "Error during adding item in data store.  Sorry mate.\t", err)
		return
	}

	fmt.Fprintf(w, "Added: %s with key %s", griddler, k) //If no error putting in, send back a success message.
}

func topHandler(w http.ResponseWriter, r *http.Request) {
	//Get top ranked griddlers from datastore.
	c := appengine.NewContext(r)
	q := datastore.NewQuery("Griddler").Order("-Rank").Limit(6)
	buff := bytes.NewBuffer(nil)
	index := 1
	for g := q.Run(c); ; index++ {
		var x Griddler
		_, err := g.Next(&x)
		if err == datastore.Done {
			break
		}
		if err != nil {
			fmt.Fprintln(buff, "Bad stuff happened in the top handler\t", err)
			return
		}
		fmt.Fprintf(buff, "%d. %s\n\n", index, x)
	}
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	io.Copy(w, buff)
}

func searchHandler(w http.ResponseWriter, r *http.Request) {
	//Get URL params.
	c := appengine.NewContext(r)
	parsed, err := url.Parse(r.URL.String())
	if err != nil {
		fmt.Fprintln(w, "Bad stuff happened in the search handler 1\t")
		return
	}
	urlParse := parsed.Query() //Parse the URL.
	search := urlParse.Get("q")
	//offset, err := strconv.Atoi(urlParse.Get("offset"))
	gtQuery := datastore.NewQuery("GriddlerTag").Filter("Tag =", search).Limit(100)
	index := 1
	for g1 := gtQuery.Run(c); ; index++ {
		var gtTemp GriddlerTag
		_, err := g1.Next(&gtTemp)
		if err == datastore.Done {
			break
		}
		if err != nil {
			fmt.Fprint(w, err)
			return
		}
		//For each GriddlerTag, get the GriddlerId and print out its equal Griddler.
		gQuery := datastore.NewQuery("Griddler").Filter("Id =", gtTemp.GriddlerId)
		for g2 := gQuery.Run(c); ; {
			var gTemp Griddler
			g2.Next(&gTemp)
			fmt.Fprint(w, gTemp)
			break
		}
		//fmt.Fprintf(buff, "%d. %s\n\n", index, gtTemp)
	}
}

func voteHandler(w http.ResponseWriter, r *http.Request) {
	//Find Griddler, save it, then delete it in data store, add vote, and done.
	c := appengine.NewContext(r)
	parsed, err := url.Parse(r.URL.String())
	if err != nil {
		fmt.Fprintln(w, "Vote Handler: ", err)
		return
	}
	q := parsed.Query() //Parse the URL.
	score := q.Get("rank")
	id := q.Get("id")

	k := datastore.NewKey(c, "Griddler", id, 0, nil)
	g := new(Griddler)
	if err := datastore.Get(c, k, g); err != nil {
		//Error.
		fmt.Fprintln(w, "Error getting griddler from data store in vote: ", err)
		return
	}
	one, err := strconv.Atoi(g.Rank)
	two, err := strconv.Atoi(score)
	g.Rank = strconv.Itoa(one + two)
	if err := datastore.Delete(c, k); err != nil {
		fmt.Fprintln(w, "Error deleting existing Griddler.")
		return
	}
	if _, err := datastore.Put(c, k, g); err != nil {
		fmt.Fprint(w, "Error during re-adding item in data store.  Sorry mate.\t", err)
		return
	}
	fmt.Fprintln(w, "Successful update.")
}

func userHandler(w http.ResponseWriter, r *http.Request) {
	//Create User at start of app.
	c := appengine.NewContext(r)
	parsed, err := url.Parse(r.URL.String())
	if err != nil {
		fmt.Fprintln(w, "User Handler: ", err)
	}
	q := parsed.Query()
	k := datastore.NewKey(c, "GriddlerUser", q.Get("u"), 0, nil)
	u := new(GriddlerUser)
	u.Username = q.Get("u")
	if _, err := datastore.Put(c, k, u); err != nil {
		fmt.Fprintln(w, "Putting User: ", err)
		return
	}
	fmt.Fprintln(w, "Successfully added ", q.Get("u"))
}

func init() {
	http.HandleFunc("/user", userHandler)
	http.HandleFunc("/create", createHandler)
	http.HandleFunc("/search", searchHandler)
	http.HandleFunc("/top", topHandler)
	http.HandleFunc("/vote", voteHandler)
}
