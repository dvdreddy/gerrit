// Copyright (C) 2015 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.extensions.api.groups;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gerrit.extensions.client.ListGroupsOption;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Groups {
  /**
   * Look up a group by ID.
   *
   * <p><strong>Note:</strong> This method eagerly reads the group. Methods that mutate the group do
   * not necessarily re-read the group. Therefore, calling a getter method on an instance after
   * calling a mutation method on that same instance is not guaranteed to reflect the mutation. It
   * is not recommended to store references to {@code groupApi} instances.
   *
   * @param id any identifier supported by the REST API, including group name or UUID.
   * @return API for accessing the group.
   * @throws RestApiException if an error occurred.
   */
  GroupApi id(String id) throws RestApiException;

  /** Create a new group with the given name and default options. */
  @CanIgnoreReturnValue
  GroupApi create(String name) throws RestApiException;

  /** Create a new group. */
  @CanIgnoreReturnValue
  GroupApi create(GroupInput input) throws RestApiException;

  /** Returns new request for listing groups. */
  ListRequest list();

  /**
   * Query groups.
   *
   * <p>Example code: {@code query().withQuery("inname:test").withLimit(10).get()}
   *
   * @return API for setting parameters and getting result.
   */
  QueryRequest query();

  /**
   * Query groups.
   *
   * <p>Shortcut API for {@code query().withQuery(String)}.
   *
   * @see #query()
   */
  QueryRequest query(String query);

  abstract class ListRequest {
    private final EnumSet<ListGroupsOption> options = EnumSet.noneOf(ListGroupsOption.class);
    private final List<String> projects = new ArrayList<>();
    private final List<String> groups = new ArrayList<>();

    private boolean visibleToAll;
    private String user;
    private boolean owned;
    private int limit;
    private int start;
    private String substring;
    private String suggest;
    private String regex;
    private String ownedBy;

    public List<GroupInfo> get() throws RestApiException {
      Map<String, GroupInfo> map = getAsMap();
      List<GroupInfo> result = new ArrayList<>(map.size());
      for (Map.Entry<String, GroupInfo> e : map.entrySet()) {
        // ListGroups "helpfully" nulls out names when converting to a map.
        e.getValue().name = e.getKey();
        result.add(e.getValue());
      }
      return Collections.unmodifiableList(result);
    }

    public abstract Map<String, GroupInfo> getAsMap() throws RestApiException;

    public ListRequest addOption(ListGroupsOption option) {
      options.add(option);
      return this;
    }

    public ListRequest addOptions(ListGroupsOption... options) {
      return addOptions(Arrays.asList(options));
    }

    public ListRequest addOptions(Iterable<ListGroupsOption> options) {
      for (ListGroupsOption option : options) {
        this.options.add(option);
      }
      return this;
    }

    public ListRequest withProject(String project) {
      projects.add(project);
      return this;
    }

    public ListRequest addGroup(String uuid) {
      groups.add(uuid);
      return this;
    }

    public ListRequest withVisibleToAll(boolean visible) {
      visibleToAll = visible;
      return this;
    }

    public ListRequest withUser(String user) {
      this.user = user;
      return this;
    }

    public ListRequest withOwned(boolean owned) {
      this.owned = owned;
      return this;
    }

    public ListRequest withLimit(int limit) {
      this.limit = limit;
      return this;
    }

    public ListRequest withStart(int start) {
      this.start = start;
      return this;
    }

    public ListRequest withSubstring(String substring) {
      this.substring = substring;
      return this;
    }

    public ListRequest withRegex(String regex) {
      this.regex = regex;
      return this;
    }

    public ListRequest withSuggest(String suggest) {
      this.suggest = suggest;
      return this;
    }

    public ListRequest withOwnedBy(String ownedBy) {
      this.ownedBy = ownedBy;
      return this;
    }

    public Set<ListGroupsOption> getOptions() {
      return options;
    }

    public List<String> getProjects() {
      return Collections.unmodifiableList(projects);
    }

    public List<String> getGroups() {
      return Collections.unmodifiableList(groups);
    }

    public boolean getVisibleToAll() {
      return visibleToAll;
    }

    public String getUser() {
      return user;
    }

    public boolean getOwned() {
      return owned;
    }

    public int getLimit() {
      return limit;
    }

    public int getStart() {
      return start;
    }

    public String getSubstring() {
      return substring;
    }

    public String getRegex() {
      return regex;
    }

    public String getSuggest() {
      return suggest;
    }

    public String getOwnedBy() {
      return ownedBy;
    }
  }

  /**
   * API for setting parameters and getting result. Used for {@code query()}.
   *
   * @see #query()
   */
  abstract class QueryRequest {
    private String query;
    private int limit;
    private int start;
    private Set<ListGroupsOption> options = EnumSet.noneOf(ListGroupsOption.class);

    /** Execute query and returns the matched groups as list. */
    public abstract List<GroupInfo> get() throws RestApiException;

    /**
     * Set query.
     *
     * @param query needs to be in human-readable form.
     */
    public QueryRequest withQuery(String query) {
      this.query = query;
      return this;
    }

    /**
     * Set limit for returned list of groups. Optional; server-default is used when not provided.
     */
    public QueryRequest withLimit(int limit) {
      this.limit = limit;
      return this;
    }

    /** Set number of groups to skip. Optional; no groups are skipped when not provided. */
    public QueryRequest withStart(int start) {
      this.start = start;
      return this;
    }

    /** Set an option on the request, appending to existing options. */
    public QueryRequest withOption(ListGroupsOption options) {
      this.options.add(options);
      return this;
    }

    /** Set options on the request, appending to existing options. */
    public QueryRequest withOptions(ListGroupsOption... options) {
      this.options.addAll(Arrays.asList(options));
      return this;
    }

    /** Set options on the request, replacing existing options. */
    public QueryRequest withOptions(Set<ListGroupsOption> options) {
      this.options = options;
      return this;
    }

    public String getQuery() {
      return query;
    }

    public int getLimit() {
      return limit;
    }

    public int getStart() {
      return start;
    }

    public Set<ListGroupsOption> getOptions() {
      return options;
    }
  }
}
