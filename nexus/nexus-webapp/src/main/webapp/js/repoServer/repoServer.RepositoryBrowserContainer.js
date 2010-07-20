// This container will host both the repository browser and the artifact
// information panel
Sonatype.repoServer.RepositoryBrowserContainer = function(config) {
  var config = config || {};
  var defaultConfig = {
    artifactContainerInitEvent : 'fileContainerInit',
    artifactContainerUpdateEvent : 'fileContainerUpdate'
  };
  Ext.apply(this, config, defaultConfig);

  var items = [];

  this.repositoryBrowser = new Sonatype.repoServer.RepositoryBrowsePanel({
        name : 'repositoryBrowser',
        payload : this.payload,
        tabTitle : this.tabTitle,
        browseIndex : false,
        region : 'center',
        nodeClickEvent : 'fileNodeClickedEvent',
        nodeClickPassthru : {
          container : this
        }
      });

  this.artifactContainer = new Sonatype.repoServer.ArtifactContainer({
        collapsible : true,
        collapsed : true,
        region : 'east',
        split : true,
        width : '50%',
        initEventName : this.artifactContainerInitEvent,
        updateEventName : this.artifactContainerUpdateEvent
      });

  items.push(this.repositoryBrowser);
  items.push(this.artifactContainer);

  Sonatype.repoServer.RepositoryBrowserContainer.superclass.constructor.call(this, {
        layout : 'border',
        // this hideMode causes the tab to properly render when coming back from
        // hidden
        hideMode : 'offsets',
        items : items
      });
};

Ext.extend(Sonatype.repoServer.RepositoryBrowserContainer, Ext.Panel, {
      updatePayload : function(payload) {

        if (payload == null)
        {
          this.collapse();
          this.repositoryBrowser.updatePayload(null);
          this.artifactContainer.collapsePanel();
        }
        else
        {
          this.expand();
          this.repositoryBrowser.updatePayload(payload);
          this.artifactContainer.updateArtifact(payload);
        }

      }
    });

// Add the browse storage and browse index panels to the repo
Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
      if (rec.data.resourceURI)
      {
        cardPanel.add(new Sonatype.repoServer.RepositoryBrowserContainer({
              payload : rec,
              name : 'browsestorage',
              tabTitle : 'Browse Storage'
            }));
      }
    });

Sonatype.Events.addListener('fileNodeClickedEvent', function(node, passthru) {
      if (passthru && passthru.container && passthru.container.artifactContainer.items.getCount() > 0)
      {
        if (node && node.isLeaf())
        {
          passthru.container.artifactContainer.updateArtifact({
                text : node.attributes.text,
                leaf : node.attributes.leaf,
                resourceURI : node.attributes.resourceURI
              });
        }
        else
        {
          passthru.container.artifactContainer.collapse();
        }
      }
    });
