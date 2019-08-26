(ns app.ui.root
  (:require
    [app.model.session :as session]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button]]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [camel-snake-kebab.core :as csk]
    [hickory.core :as hc]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [clojure.string :as str]))

;; ;;;;;;;;;;;;;;;;
;; ;; Utils
;; ;;;;;;;;;;;;;;;;

;; (defn field [{:keys [label valid? error-message] :as props}]
;;   (let [input-props (-> props (assoc :name label) (dissoc :label :valid? :error-message))]
;;     (div :.ui.field
;;       (dom/label {:htmlFor label} label)
;;       (dom/input input-props)
;;       (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
;;         error-message))))

;; ;;;;;;;;;;;;;;;;
;; ;; SignUp
;; ;;;;;;;;;;;;;;;;

;; (defsc SignupSuccess [this props]
;;   {:query         ['*]
;;    :initial-state {}
;;    :ident         (fn [] [:component/id :signup-success])
;;    :route-segment ["signup-success"]
;;    :will-enter    (fn [app _] (dr/route-immediate [:component/id :signup-success]))}
;;   (div
;;     (dom/h3 "Signup Complete!")
;;     (dom/p "You can now log in!")))

;; (defsc Signup [this {:account/keys [email password password-again] :as props}]
;;   {:query             [:account/email :account/password :account/password-again fs/form-config-join]
;;    :initial-state     (fn [_]
;;                         (fs/add-form-config Signup
;;                           {:account/email          ""
;;                            :account/password       ""
;;                            :account/password-again ""}))
;;    :form-fields       #{:account/email :account/password :account/password-again}
;;    :ident             (fn [] session/signup-ident)
;;    :route-segment     ["signup"]
;;    :componentDidMount (fn [this]
;;                         (comp/transact! this [(session/clear-signup-form)]))
;;    :will-enter        (fn [app _] (dr/route-immediate [:component/id :signup]))}
;;   (let [submit!  (fn [evt]
;;                    (when (or (identical? true evt) (evt/enter-key? evt))
;;                      (comp/transact! this [(session/signup! {:email email :password password})])
;;                      (log/info "Sign up")))
;;         checked? (log/spy :info (fs/checked? props))]
;;     (div
;;       (dom/h3 "Signup")
;;       (div :.ui.form {:classes [(when checked? "error")]}
;;         (field {:label         "Email"
;;                 :value         (or email "")
;;                 :valid?        (session/valid-email? email)
;;                 :error-message "Must be an email address"
;;                 :autoComplete  "off"
;;                 :onKeyDown     submit!
;;                 :onChange      #(m/set-string! this :account/email :event %)})
;;         (field {:label         "Password"
;;                 :type          "password"
;;                 :value         (or password "")
;;                 :valid?        (session/valid-password? password)
;;                 :error-message "Password must be at least 8 characters."
;;                 :onKeyDown     submit!
;;                 :autoComplete  "off"
;;                 :onChange      #(m/set-string! this :account/password :event %)})
;;         (field {:label         "Repeat Password" :type "password" :value (or password-again "")
;;                 :autoComplete  "off"
;;                 :valid?        (= password password-again)
;;                 :error-message "Passwords do not match."
;;                 :onChange      #(m/set-string! this :account/password-again :event %)})
;;         (dom/button :.ui.primary.button {:onClick #(submit! true)}
;;           "Sign Up")))))

;; (declare Session)

;; ;;;;;;;;;;;;;;;;
;; ;; LogIn
;; ;;;;;;;;;;;;;;;;

;; (defsc Login [this {:account/keys [email]
;;                     :ui/keys      [error open?] :as props}]
;;   {:query         [:ui/open? :ui/error :account/email
;;                    {[:component/id :session] (comp/get-query Session)}
;;                    [::uism/asm-id ::session/session]]
;;    :css           [[:.floating-menu {:position "absolute !important"
;;                                      :z-index  1000
;;                                      :width    "300px"
;;                                      :right    "0px"
;;                                      :top      "50px"}]]
;;    :initial-state {:account/email "" :ui/error ""}
;;    :ident         (fn [] [:component/id :login])}
;;   (let [current-state (uism/get-active-state this ::session/session)
;;         {current-user :account/name} (get props [:component/id :session])
;;         initial?      (= :initial current-state)
;;         loading?      (= :state/checking-session current-state)
;;         logged-in?    (= :state/logged-in current-state)
;;         {:keys [floating-menu]} (css/get-classnames Login)
;;         password      (or (comp/get-state this :password) "")] ; c.l. state for security
;;     (dom/div
;;       (when-not initial?
;;         (dom/div :.right.menu
;;           (if logged-in?
;;             (dom/button :.item
;;               {:onClick #(uism/trigger! this ::session/session :event/logout)}
;;               (dom/span current-user) ent/nbsp "Log out")
;;             (dom/div :.item {:style   {:position "relative"}
;;                              :onClick #(uism/trigger! this ::session/session :event/toggle-modal)}
;;               "Login"
;;               (when open?
;;                 (dom/div :.four.wide.ui.raised.teal.segment {:onClick (fn [e]
;;                                                                         ;; Stop bubbling (would trigger the menu toggle)
;;                                                                         (evt/stop-propagation! e))
;;                                                              :classes [floating-menu]}
;;                   (dom/h3 :.ui.header "Login")
;;                   (div :.ui.form {:classes [(when (seq error) "error")]}
;;                     (field {:label    "Email"
;;                             :value    email
;;                             :onChange #(m/set-string! this :account/email :event %)})
;;                     (field {:label    "Password"
;;                             :type     "password"
;;                             :value    password
;;                             :onChange #(comp/set-state! this {:password (evt/target-value %)})})
;;                     (div :.ui.error.message error)
;;                     (div :.ui.field
;;                       (dom/button :.ui.button
;;                         {:onClick (fn [] (uism/trigger! this ::session/session :event/login {:username email
;;                                                                                              :password password}))
;;                          :classes [(when loading? "loading")]} "Login"))
;;                     (div :.ui.message
;;                       (dom/p "Don't have an account?")
;;                       (dom/a {:onClick (fn []
;;                                          (uism/trigger! this ::session/session :event/toggle-modal {})
;;                                          (dr/change-route this ["signup"]))}
;;                         "Please sign up!"))))))))))))

;; (def ui-login (comp/factory Login))

;; ;;;;;;;;;;;;;;;;
;; ;; Main
;; ;;;;;;;;;;;;;;;;


;; (defsc Main [this props]
;;   {:query         [:main/welcome-message]
;;    :initial-state {:main/welcome-message "Hi!"}
;;    :ident         (fn [] [:component/id :main])
;;    :route-segment ["main"]
;;    :will-enter    (fn [_ _] (dr/route-immediate [:component/id :main]))}
;;   (div :.ui.container.segment
;;     (h3 "Main")))




;; ;;;;;;;;;;;;;;;;
;; ;; Settings
;; ;;;;;;;;;;;;;;;;

;; (defsc Settings [this {:keys [:account/time-zone :account/real-name] :as props}]
;;   {:query         [:account/time-zone :account/real-name]
;;    :ident         (fn [] [:component/id :settings])
;;    :route-segment ["settings"]
;;    :will-enter    (fn [_ _] (dr/route-immediate [:component/id :settings]))
;;    :initial-state {}}
;;   (div :.ui.container.segment
;;     (h3 "Settings")))

;; (dr/defrouter TopRouter [this props]
;;   {:router-targets [Main Signup SignupSuccess Settings]})

;; (def ui-top-router (comp/factory TopRouter))

;; ;;;;;;;;;;;;;;;;
;; ;; Session
;; ;;;;;;;;;;;;;;;;

;; (defsc Session
;;   "Session representation. Used primarily for server queries. On-screen representation happens in Login component."
;;   [this {:keys [:session/valid? :account/name] :as props}]
;;   {:query         [:session/valid? :account/name]
;;    :ident         (fn [] [:component/id :session])
;;    :pre-merge     (fn [{:keys [data-tree]}]
;;                     (merge {:session/valid? false :account/name ""}
;;                       data-tree))
;;    :initial-state {:session/valid? false :account/name ""}})

;; (def ui-session (prim/factory Session))

;; ;;;;;;;;;;;;;;;;
;; ;; TopChrome
;; ;;;;;;;;;;;;;;;;

;; (defsc TopChrome [this {:root/keys [router current-session login]}]
;;   {:query         [{:root/router (comp/get-query TopRouter)}
;;                    {:root/current-session (comp/get-query Session)}
;;                    [::uism/asm-id ::TopRouter]
;;                    {:root/login (comp/get-query Login)}]
;;    :ident         (fn [] [:component/id :top-chrome])
;;    :initial-state {:root/router          {}
;;                    :root/login           {}
;;                    :root/current-session {}}}
;;   (let [current-tab (some-> (dr/current-route this this) first keyword)]
;;     (div :.ui.container
;;       (div :.ui.secondary.pointing.menu
;;         (dom/a :.item {:classes [(when (= :main current-tab) "active")]
;;                        :onClick (fn [] (dr/change-route this ["main"]))} "Main")
;;         (dom/a :.item {:classes [(when (= :settings current-tab) "active")]
;;                        :onClick (fn [] (dr/change-route this ["settings"]))} "Settings")
;;         (div :.right.menu
;;           (ui-login login)))
;;       (div :.ui.grid
;;         (div :.ui.row
;;           (ui-top-router router))))))

;; (def ui-top-chrome (comp/factory TopChrome))

;; ;;;;;;;;;;;;;;;;
;; ;; Bump Number
;; ;;;;;;;;;;;;;;;;

;; (defmutation bump-number [ignored]
;;   (action [{:keys [state]}]
;;           (swap! state update :root/number inc)))

;; (defsc BumpNumber [this {:root/keys [number]}]
;;        {:query         [:root/number]
;;         :initial-state {:root/number 0}}
;;        (dom/div
;;          (dom/h4 "This is an example.")
;;          (dom/button {:onClick #(comp/transact! this `[(bump-number {})])}
;;                      "You've clicked this button " number " times.")))

;; (def ui-bump-number (comp/factory BumpNumber))

;; ;;;;;;;;;;;;;;;;
;; ;; ROOT
;; ;;;;;;;;;;;;;;;;


;; (defsc Root [this {:root/keys [top-chrome]}]
;;   {:query             [{:root/top-chrome (comp/get-query TopChrome)}]
;;    :ident             (fn [] [:component/id :ROOT])
;;    :initial-state     {:root/top-chrome {}}}
;;   (div
;;     (ui-top-chrome top-chrome)
;;     (ui-bump-number {})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;
;; VANILLA EXAMPLES
;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;
;; HTML Converter
;;;;;;;;;;;;;;;;


(def attr-renames {:class        :className
                   :for          :htmlFor
                   :tabindex     :tabIndex
                   :viewbox      :viewBox
                   :spellcheck   :spellcheck
                   :autocorrect  :autoCorrect
                   :autocomplete :autoComplete})

(defn fix-style [style]
      (try
        (let [lines     (str/split style #";")
              style-map (into {} (map (fn [line]
                                          (let [[k v] (str/split line #":")]
                                               [(csk/->camelCase (keyword k)) (str/trim v)])) lines))]

             style-map)
        (catch #?(:cljs :default :clj Exception) e
          style)))

(defn classes->keyword [className]
      (when (seq (str/trim className))
            (let [classes (keep (fn [e] (when (seq e) e)) (str/split className #"  *"))
                  kw      (keyword (str "." (str/join "." classes)))]
                 kw)))

(defn- chars->entity [ns chars]
       (if (= \# (first chars))
         (apply str "&" (conj chars ";"))                        ; skip it. needs (parse int, convert base, format to 4-digit code)
         (if (seq ns)
           (symbol ns (apply str chars))
           (symbol (apply str chars)))))

(defn- parse-entity [stream result {:keys [entity-ns] :as options}]
       (loop [s stream chars []]
             (let [c (first s)]
                  (case c
                        (\; nil) [(rest s) (if (seq chars)
                                             (conj result (chars->entity entity-ns chars))
                                             result)]
                        (recur (rest s) (conj chars c))))))

(defn- html-string->react-string [html-str {:keys [ignore-entities?] :as options}]
       (if ignore-entities?
         html-str
         (loop [s html-str result []]
               (let [c (first s)
                     [new-stream new-result] (case c
                                                   nil [nil result]
                                                   \& (parse-entity (rest s) result options)
                                                   [(rest s) (conj result c)])]
                    (if new-stream
                      (recur new-stream new-result)
                      (let [segments (partition-by char? new-result)
                            result   (mapv (fn [s]
                                               (if (char? (first s))
                                                 (apply str s)
                                                 (first s))) segments)]
                           result))))))

(defn element->call
      ([elem]
       (element->call elem {}))
      ([elem {:keys [ns-alias keep-empty-attrs?] :as options}]
       (cond
         (and (string? elem)
              (let [elem (str/trim elem)]
                   (or
                     (= "" elem)
                     (and
                       (str/starts-with? elem "<!--")
                       (str/ends-with? elem "-->"))
                     (re-matches #"^[ \n]*$" elem)))) nil
         (string? elem) (html-string->react-string (str/trim elem) options)
         (vector? elem) (let [tag               (name (first elem))
                              raw-props         (second elem)
                              classkey          (when (contains? raw-props :class)
                                                      (classes->keyword (:class raw-props)))
                              attrs             (cond-> (set/rename-keys raw-props attr-renames)
                                                        (contains? raw-props :class) (dissoc :className)
                                                        (contains? raw-props :style) (update :style fix-style))
                              children          (keep (fn [c] (element->call c options)) (drop 2 elem))
                              expanded-children (reduce
                                                  (fn [acc c]
                                                      (if (vector? c)
                                                        (into [] (concat acc c))
                                                        (conj acc c)))
                                                  []
                                                  children)]
                             (concat (list) (keep identity
                                                  [(if (seq ns-alias)
                                                     (symbol ns-alias tag)
                                                     (symbol tag))
                                                   (when classkey classkey)
                                                   (if keep-empty-attrs?
                                                     attrs
                                                     (when (seq attrs) attrs))]) expanded-children))
         :otherwise "")))

(defn html->clj-dom
      "Convert an HTML fragment (containing just one tag) into a corresponding Dom cljs.

      Options is a map that can contain:

      - `ns-alias`: The primary DOM namespace alias to use.  If not set, the calls will not be namespaced.
      - `keep-empty-attrs?`: Boolean (default false). Output (dom/p {} ...) vs (dom/p ...).
      - `entity-ns`: String (defaults to \"ent\"). When named HTML entities are found they are converted to the Fulcro
        HTML entity ns symbols that stand for the correct unicode (e.g. \"&quot;\" -> `ent/quot`). This is the ns alias
        for those.
      - `ignore-entities?`: Boolean (default false). If true, entities in strings will not be touched.
      "
      ([html-fragment options]
       (let [hiccup-list (map hc/as-hiccup (hc/parse-fragment html-fragment))
             options     (cond-> options
                                 (not (contains? options :entity-ns)) (assoc :entity-ns "ent"))]
            (let [result (keep (fn [e] (element->call e options)) hiccup-list)]
                 (if (< 1 (count result))
                   (vec result)
                   (first result)))))
      ([html-fragment]
       (html->clj-dom html-fragment {:ns-alias "dom"})))

(defmutation convert [p]
             (action [{:keys [state]}]
                     (let [html (get-in @state [:top :conv :html])
                           cljs (html->clj-dom html)]
                          (swap! state assoc-in [:top :conv :cljs] {:code cljs}))))

(defsc HTMLConverter [this {:keys [html cljs]}]
       {:initial-state (fn [params] {:html "<div id=\"3\" class=\"b\"><p>Paragraph</p></div>" :cljs {:code (list)}})
        :query         [:cljs :html]
        :ident         (fn [] [:top :conv])}
       (dom/div {:className ""}
                (dom/textarea {:cols     80 :rows 10
                               :onChange (fn [evt] (m/set-string! this :html :event evt))
                               :value    html})
                (dom/button :.c-button {:onClick (fn [evt]
                                                     (comp/transact! this `[(convert {})]))} "Convert")
                (dom/pre {} (with-out-str (pprint (:code cljs))))))

(def ui-html-convert (comp/factory HTMLConverter))

(defsc Root [this {:keys [converter]}]
       {:initial-state {:converter {}}
        :query         [{:converter (comp/get-query HTMLConverter)}]}
       (ui-html-convert converter))



