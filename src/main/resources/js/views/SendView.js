(function(coinswap) {

coinswap.SendView = Backbone.View.extend({
  events: {
    'blur .address .value': 'validateAddress',
    'blur .amount .value': 'validateAmount',
    'click .send': 'send',
    'click .address .paste': 'pasteAddress'
  },

  template: $('#template-send').html(),
  modalTemplate: _.template($('#template-send-modal').html()),
  className: 'send',

  initialize: function(options) {
    _.bindAll(this, 'render', 'validateAddress',
      'validateAmount', 'send');

    this.render();

    if(options.id)
      this.setCurrency(options.id);
  },

  render: function() {
    this.$el.html(this.template);
    this.$el.find('[data-toggle="tooltip"]').tooltip({
      animation: false,
      container: this.$el,
      placement: 'bottom'
    });
    this.delegateEvents();
  },

  setCurrency: function(id) {
    this.$el.find('.currency').text(id);
    var altEl = this.$el.find('.amount .alt');
    if(id) altEl.removeClass('hidden');
    else altEl.addClass('hidden');
  },

  validateAddress: function(e) {
    var addressEl = this.$el.find('.address');
    var address = addressEl.find('.value').val().trim();
    var currency;

    if(address) {
      for(var i = 0; i < this.collection.length; i++) {
        var coin = this.collection.at(i);
        if(coin.isAddressValid(address)) {
          currency = coin.id;
          this.model = coin;
          break;
        }
      }
    }
    this.setCurrency(currency);

    addressEl[currency || !address ? 'removeClass' : 'addClass']('has-error');
  },

  validateAmount: function(e) {
    var amountEl = this.$el.find('.amount');
    var amount = amountEl.find('.value').val().trim();
    if(!+amount || +amount < 0) amountEl.addClass('has-error');
    else amountEl.removeClass('has-error');
  },

  send: function() {
    var t = this;
    t.validateAddress();
    t.validateAmount();

    var addressEl = t.$el.find('.address');
    var amountEl = t.$el.find('.amount');
    if(addressEl.hasClass('has-error') || amountEl.hasClass('has-error')
    || !addressEl.find('.value').val() || !amountEl.find('.value').val())
      return;

    var address = addressEl.find('.value').val().trim();
    var amount = amountEl.find('.value').val().trim();

    var modal = $('#modal');
    modal.find('.modal-content').html(t.modalTemplate({
      address: address,
      amount: amount,
      currency: t.model.attributes,
      sent: false
    }));
    modal.modal('show');

    modal.find('.confirm').click(function(e) {
      t.model.send(address, +amount);
      
      modal.find('.modal-content').html(t.modalTemplate({
        address: address,
        amount: amount,
        currency: t.model.attributes,
        sent: true
      }));

      modal.find('.view-tx').click(function() {
        modal.modal('hide');
      })
    });

  },

  pasteAddress: function() {
    var address = clipboard.get().trim();
    if(!address) return;
    this.$el.find('.address .value').val(address);
    this.validateAddress();
  }
});

})(coinswap);
